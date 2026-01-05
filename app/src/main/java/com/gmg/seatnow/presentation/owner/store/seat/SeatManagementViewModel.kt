package com.gmg.seatnow.presentation.owner.store.seat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.FloorCategory
import com.gmg.seatnow.domain.model.TableItem
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
    private val updateSeatUsageUseCase: UpdateSeatUsageUseCase
) : ViewModel() {
    enum class SeatDisplayMode {
        EMPTY,      // 빈 좌석 보기
        OCCUPIED    // 이용 좌석 보기
    }

    // 2. UI State
    data class SeatManagementUiState(
        val categories: List<FloorCategory> = emptyList(),
        val selectedCategoryId: String = "ALL",
        val displayItems: List<TableItem> = emptyList(), // 현재 화면에 보여줄 리스트
        val totalSeatCapacity: Int = 0,
        val currentUsedSeats: Int = 0,
        val displayMode: SeatDisplayMode = SeatDisplayMode.EMPTY,
        val isSaving: Boolean = false,
        val isEditMode: Boolean = false
    )

    private val _uiState = MutableStateFlow(SeatManagementUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<SeatManagementEvent>()
    val event: SharedFlow<SeatManagementEvent> = _event.asSharedFlow()

    // ★ [핵심] 원본 데이터 소스 (Source of Truth)
    // 층별 구분을 위해 실제 데이터는 여기서 관리하고, UI에는 가공해서 뿌립니다.
    private var _allRawTables: List<TableItem> = emptyList()

    init {
        loadMockData()
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
            // ★ [추가] 업데이트 버튼 클릭 시 -> 수정 모드 진입
            is SeatManagementAction.OnUpdateClick -> {
                _uiState.update { it.copy(isEditMode = true) }
            }
            // ★ [수정] 저장 버튼 클릭 시 -> API 호출 후 조회 모드로 복귀
            is SeatManagementAction.OnSaveClick -> saveSeatData()
        }
    }

    private fun saveSeatData() {
        // 1. 로딩 시작
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            // 2. UseCase 호출 (현재 원본 데이터인 _allRawTables를 서버로 전송)
            // 참고: displayItems는 필터링된 리스트일 수 있으므로 전체 데이터를 보내는 것이 안전합니다.
            // API 스펙에 따라 _allRawTables 혹은 현재 수정된 항목들만 보낼 수도 있습니다.
            val result = updateSeatUsageUseCase(_allRawTables)

            // 3. 결과 처리
            result.onSuccess {
                // 저장 성공: 로딩 해제, 수정 모드 종료, 성공 메시지
                _uiState.update { state ->
                    state.copy(isSaving = false, isEditMode = false)
                }
                _event.emit(SeatManagementEvent.ShowToast("저장되었습니다."))
            }.onFailure { exception ->
                // 저장 실패: 로딩만 해제하고 에러 메시지 (수정 모드 유지)
                _uiState.update { state ->
                    state.copy(isSaving = false)
                }
                _event.emit(SeatManagementEvent.ShowToast("저장에 실패했습니다. 다시 시도해주세요."))
            }
        }
    }

    private fun loadMockData() {
        // ★ [요청사항 반영] 초기값(currentCount)은 모두 0으로 설정
        val floor1Tables = listOf(
            TableItem("1F_4", "4인 테이블", 4, 2, 0),
            TableItem("1F_2", "2인 테이블", 2, 4, 0)
        )
        val floor2Tables = listOf(
            TableItem("2F_4", "4인 테이블", 4, 4, 0),
            TableItem("2F_2", "2인 테이블", 2, 2, 0)
        )

        _allRawTables = floor1Tables + floor2Tables

        val categories = listOf(
            FloorCategory("ALL", "전체"),
            FloorCategory("1F", "1층"),
            FloorCategory("2F", "2층")
        )

        _uiState.update {
            it.copy(
                categories = categories,
                selectedCategoryId = "ALL"
            )
        }
        updateDisplayItems() // 초기 데이터 로드 후 화면 갱신
    }

    // ★ [핵심 로직] 현재 선택된 카테고리에 맞춰 데이터를 가공하는 함수
    private fun updateDisplayItems() {
        val currentCategory = _uiState.value.selectedCategoryId

        val itemsToShow = if (currentCategory == "ALL") {
            // [1] '전체' 탭일 경우: 같은 라벨(이름)을 가진 테이블끼리 합칩니다 (Aggregate).
            _allRawTables
                .groupBy { it.label } // 이름으로 그룹핑
                .map { (label, tables) ->
                    TableItem(
                        id = "MERGED_$label", // 합쳐진 항목임을 식별하기 위한 ID
                        label = label,
                        capacityPerTable = tables.first().capacityPerTable,
                        maxTableCount = tables.sumOf { it.maxTableCount }, // 총 보유량 합산
                        currentCount = tables.sumOf { it.currentCount }    // 현재 사용량 합산
                    )
                }
                .sortedByDescending { it.capacityPerTable } // 인원수 많은 순 정렬 (4인 -> 2인)
        } else {
            // [2] 층별 탭일 경우: ID 접두사(1F_, 2F_)를 보고 필터링
            _allRawTables.filter { it.id.startsWith("${currentCategory}_") }
        }

        // 전체 통계 계산 (원본 데이터 기준)
        val totalCapacity = _allRawTables.sumOf { it.maxTableCount * it.capacityPerTable }
        val currentUsed = _allRawTables.sumOf { it.currentCount * it.capacityPerTable }

        _uiState.update {
            it.copy(
                displayItems = itemsToShow,
                totalSeatCapacity = totalCapacity,
                currentUsedSeats = currentUsed
            )
        }
    }

    // ★ [핵심 로직] 테이블 증감 처리 (합쳐진 아이템 처리 포함)
    private fun processTableUpdate(itemId: String, delta: Int) {
        // 원본 리스트를 복사해서 수정 준비
        val newRawList = _allRawTables.toMutableList()

        if (itemId.startsWith("MERGED_")) {
            // [CASE A] 합쳐진 아이템(ALL 탭)에서 클릭했을 때
            val targetLabel = itemId.removePrefix("MERGED_")

            // 해당 라벨을 가진 실제 테이블들을 찾음
            val targets = newRawList.filter { it.label == targetLabel }

            if (delta > 0) {
                // 증가(+): 여유가 있는 첫 번째 테이블을 찾아 증가시킴
                val itemToUpdate = targets.firstOrNull { it.currentCount < it.maxTableCount }
                itemToUpdate?.let { item ->
                    val index = newRawList.indexOf(item)
                    newRawList[index] = item.copy(currentCount = item.currentCount + 1)
                }
            } else {
                // 감소(-): 사용 중인 첫 번째 테이블을 찾아 감소시킴
                val itemToUpdate = targets.lastOrNull { it.currentCount > 0 }
                itemToUpdate?.let { item ->
                    val index = newRawList.indexOf(item)
                    newRawList[index] = item.copy(currentCount = item.currentCount - 1)
                }
            }

        } else {
            // [CASE B] 개별 층(1F, 2F)에서 클릭했을 때 (기존 로직)
            val index = newRawList.indexOfFirst { it.id == itemId }
            if (index != -1) {
                val item = newRawList[index]
                val newCount = (item.currentCount + delta).coerceIn(0, item.maxTableCount)
                newRawList[index] = item.copy(currentCount = newCount)
            }
        }

        // 변경사항 저장 및 UI 갱신
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