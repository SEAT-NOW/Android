package com.gmg.seatnow.presentation.owner.store.seat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.FloorCategory
import com.gmg.seatnow.domain.model.TableItem
import com.gmg.seatnow.domain.usecase.store.GetSeatStatusUseCase
import com.gmg.seatnow.domain.usecase.store.UpdateSeatUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeatManagementViewModel @Inject constructor(
    private val updateSeatUsageUseCase: UpdateSeatUsageUseCase,
    private val getSeatStatusUseCase: GetSeatStatusUseCase
) : ViewModel() {
    enum class SeatDisplayMode {
        EMPTY,      // 빈 좌석 보기
        OCCUPIED    // 이용 좌석 보기
    }

    // 2. UI State
    data class SeatManagementUiState(
        val categories: List<FloorCategory> = emptyList(),
        val selectedCategoryId: String = "ALL",
        val groupedDisplayItems: Map<String, List<TableItem>> = emptyMap(),
        val totalSeatCapacity: Int = 0,
        val currentUsedSeats: Int = 0,
        val displayMode: SeatDisplayMode = SeatDisplayMode.EMPTY,
        val isSaving: Boolean = false,
        val isEditMode: Boolean = false,
        val isLoading: Boolean = false
    )

    private val _uiState = MutableStateFlow(SeatManagementUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<SeatManagementEvent>()
    val event: SharedFlow<SeatManagementEvent> = _event.asSharedFlow()

    // ★ [핵심] 원본 데이터 소스 (Source of Truth)
    private var _allRawTables: List<TableItem> = emptyList()

    init {
        loadData()
    }

    fun onAction(action: SeatManagementAction) {
        when (action) {
            is SeatManagementAction.SelectCategory -> {
                _uiState.update { it.copy(selectedCategoryId = action.categoryId) }
                updateDisplayItems() // 카테고리 변경 시 보여줄 아이템 다시 계산
            }
            is SeatManagementAction.IncrementTableCount -> processTableUpdate(action.itemId, 1)
            is SeatManagementAction.DecrementTableCount -> processTableUpdate(action.itemId, -1)
            is SeatManagementAction.ToggleDisplayMode -> {
                _uiState.update { it.copy(displayMode = action.mode) }
            }
            is SeatManagementAction.OnUpdateClick -> {
                // 그냥 현재 상태에서 수정 모드만 켭니다. (ALL 탭이면 ALL 탭인 채로 수정 모드 진입)
                _uiState.update { it.copy(isEditMode = true) }
                updateDisplayItems() // 화면 갱신 (이때 전체 합계 섹션이 사라짐)
            }
            is SeatManagementAction.OnSaveClick -> saveSeatData()
        }
    }

    private fun saveSeatData() {
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val allFloorIds = _allRawTables.map { it.floorId }.distinct()
            var isAllSuccess = true

            // 순차 처리 (await)
            for (floorId in allFloorIds) {
                val floorItems = _allRawTables.filter { it.floorId == floorId }
                val result = updateSeatUsageUseCase(floorItems)

                if (result.isFailure) {
                    isAllSuccess = false
                }
            }

            _uiState.update { it.copy(isSaving = false) }

            if (isAllSuccess) {
                _uiState.update { it.copy(isEditMode = false) }
                updateDisplayItems()
                _event.emit(SeatManagementEvent.ShowToast("저장되었습니다."))
            } else {
                _event.emit(SeatManagementEvent.ShowToast("일부 데이터 저장에 실패했습니다."))
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Repository 캐시 덕분에 화면 복귀 시 API 호출 없이 즉시 반환됨
            getSeatStatusUseCase()
                .onSuccess { data ->
                    // 1. 카테고리(층) 설정
                    // 2. 전체 테이블 데이터 저장
                    _allRawTables = data.allTables

                    _uiState.update {
                        it.copy(
                            categories = data.categories,
                            // selectedCategoryId = "ALL", // 선택 상태 초기화 방지 (기존 선택 유지)
                            isLoading = false
                        )
                    }
                    // 3. 화면 갱신
                    updateDisplayItems()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    // _event.emit(SeatManagementEvent.ShowToast(e.message ?: "로드 실패"))
                }
        }
    }

    // ★ [핵심 로직] 현재 선택된 카테고리에 맞춰 데이터를 가공하는 함수
    private fun updateDisplayItems() {
        val state = _uiState.value
        val resultMap = mutableMapOf<String, List<TableItem>>()

        if (state.selectedCategoryId == "ALL") {
            // [ALL 탭 로직]

            // 1. 전체 합계 섹션 생성 (수정 모드가 아닐 때만 보여줌!)
            if (!state.isEditMode) {
                val mergedList = _allRawTables
                    .groupBy { it.label }
                    .map { (label, items) ->
                        TableItem(
                            id = "MERGED_$label",
                            floorId = "ALL",
                            label = label,
                            capacityPerTable = items.first().capacityPerTable,
                            maxTableCount = items.sumOf { it.maxTableCount },
                            currentCount = items.sumOf { it.currentCount }
                        )
                    }
                    .sortedBy { it.capacityPerTable }

                if (mergedList.isNotEmpty()) {
                    resultMap["전체"] = mergedList
                }
            }

            // 2. 층별 섹션 생성
            val floorCategories = state.categories.filter { it.id != "ALL" }

            floorCategories.forEach { category ->
                val floorItems = _allRawTables.filter { it.floorId == category.id }
                if (floorItems.isNotEmpty()) {
                    resultMap[category.name] = floorItems
                }
            }

        } else {
            // [개별 층 탭 로직]
            val categoryName = state.categories.find { it.id == state.selectedCategoryId }?.name ?: ""
            val floorItems = _allRawTables.filter { it.floorId == state.selectedCategoryId }
            resultMap[categoryName] = floorItems
        }

        // 전체 통계
        val totalCapacity = _allRawTables.sumOf { it.capacityPerTable * it.maxTableCount }
        val usedSeats = _allRawTables.sumOf { it.capacityPerTable * it.currentCount }

        _uiState.update {
            it.copy(
                groupedDisplayItems = resultMap,
                totalSeatCapacity = totalCapacity,
                currentUsedSeats = usedSeats
            )
        }
    }
    private fun processTableUpdate(itemId: String, delta: Int) {
        // 수정 모드에서는 "ALL" 탭이 진입 불가이므로, MERGED 아이템 처리는 불필요하지만
        // 안전을 위해 일반 로직만 유지합니다.
        val newRawList = _allRawTables.toMutableList()
        val index = newRawList.indexOfFirst { it.id == itemId }

        if (index != -1) {
            val item = newRawList[index]
            val newCount = (item.currentCount + delta).coerceIn(0, item.maxTableCount)
            newRawList[index] = item.copy(currentCount = newCount)
        }

        _allRawTables = newRawList
        updateDisplayItems()
    }
}

sealed interface SeatManagementEvent {
    data class ShowToast(val message: String) : SeatManagementEvent
}

// Action 정의
sealed interface SeatManagementAction {
    data class SelectCategory(val categoryId: String) : SeatManagementAction
    data class IncrementTableCount(val itemId: String) : SeatManagementAction
    data class DecrementTableCount(val itemId: String) : SeatManagementAction
    data class ToggleDisplayMode(val mode: SeatManagementViewModel.SeatDisplayMode) : SeatManagementAction
    data object OnUpdateClick : SeatManagementAction
    data object OnSaveClick : SeatManagementAction
}