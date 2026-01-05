package com.gmg.seatnow.presentation.owner.store.seat

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SeatManagementViewModel @Inject constructor() : ViewModel() {

    // 1. 도메인/데이터 모델
    data class TableItem(
        val id: String,
        val label: String,
        val capacityPerTable: Int,
        val maxTableCount: Int,
        val currentCount: Int = 0
    )

    data class FloorCategory(
        val id: String,
        val name: String,
        val tableList: List<TableItem>
    )

    // ★ 추가: 토글 상태 관리를 위한 Enum
    enum class SeatDisplayMode {
        EMPTY,      // 빈 좌석 보기
        OCCUPIED    // 이용 좌석 보기
    }

    // 2. UI State
    data class SeatManagementUiState(
        val categories: List<FloorCategory> = emptyList(),
        val selectedCategoryId: String = "ALL",
        val displayItems: List<TableItem> = emptyList(),
        val totalSeatCapacity: Int = 0,
        val currentUsedSeats: Int = 0,
        val displayMode: SeatDisplayMode = SeatDisplayMode.EMPTY, // ★ 기본값: 빈 좌석
        val isSaving: Boolean = false
    )

    private val _uiState = MutableStateFlow(SeatManagementUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMockData()
    }

    // 3. Action 처리 (규약 준수: 모든 입력은 Action으로 처리)
    fun onAction(action: SeatManagementAction) {
        when (action) {
            is SeatManagementAction.SelectCategory -> {
                val selectedCategory = _uiState.value.categories.find { it.id == action.categoryId } ?: return
                _uiState.update {
                    it.copy(
                        selectedCategoryId = action.categoryId,
                        displayItems = selectedCategory.tableList
                    )
                }
                calculateSeatStats()
            }
            is SeatManagementAction.IncrementTableCount -> updateTableCount(action.itemId, 1)
            is SeatManagementAction.DecrementTableCount -> updateTableCount(action.itemId, -1)
            is SeatManagementAction.ToggleDisplayMode -> {
                _uiState.update { it.copy(displayMode = action.mode) }
            }
            is SeatManagementAction.OnSaveClick -> {
                _uiState.update { it.copy(isSaving = true) }
                // TODO: Save Logic
            }
        }
    }

    private fun loadMockData() {
        // 기존 Mock 데이터 로직 유지
        val floor1Tables = listOf(
            TableItem("1F_4", "4인 테이블", 4, 2, 2), // 2개 모두 사용 중으로 가정 (예시 수정)
            TableItem("1F_2", "2인 테이블", 2, 4, 1)  // 4개 중 1개 사용 중
        )
        val floor2Tables = listOf(
            TableItem("2F_4", "4인 테이블", 4, 4, 0),
            TableItem("2F_2", "2인 테이블", 2, 2, 0)
        )
        // 전체 합산
        val allTables = floor1Tables + floor2Tables

        val categories = listOf(
            FloorCategory("ALL", "전체", allTables),
            FloorCategory("1F", "1층", floor1Tables),
            FloorCategory("2F", "2층", floor2Tables)
        )

        _uiState.update {
            it.copy(
                categories = categories,
                selectedCategoryId = "ALL",
                displayItems = allTables
            )
        }
        calculateSeatStats()
    }

    private fun updateTableCount(itemId: String, delta: Int) {
        val currentList = _uiState.value.displayItems.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index == -1) return

        val item = currentList[index]
        val newCount = (item.currentCount + delta).coerceIn(0, item.maxTableCount)

        if (item.currentCount != newCount) {
            currentList[index] = item.copy(currentCount = newCount)
            _uiState.update { it.copy(displayItems = currentList) }
            calculateSeatStats()
        }
    }

    private fun calculateSeatStats() {
        val currentItems = _uiState.value.displayItems
        val totalCapacity = currentItems.sumOf { it.maxTableCount * it.capacityPerTable }
        val currentUsed = currentItems.sumOf { it.currentCount * it.capacityPerTable }

        _uiState.update {
            it.copy(
                totalSeatCapacity = totalCapacity,
                currentUsedSeats = currentUsed
            )
        }
    }
}

// ★ Action 정의 (규약 준수)
sealed interface SeatManagementAction {
    data class SelectCategory(val categoryId: String) : SeatManagementAction
    data class IncrementTableCount(val itemId: String) : SeatManagementAction
    data class DecrementTableCount(val itemId: String) : SeatManagementAction
    data class ToggleDisplayMode(val mode: SeatManagementViewModel.SeatDisplayMode) : SeatManagementAction
    data object OnSaveClick : SeatManagementAction
}