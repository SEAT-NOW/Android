package com.gmg.seatnow.presentation.owner.store.seat

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SeatManagementViewModel @Inject constructor() : ViewModel() {

    // 1. 데이터 모델 (화면 내부에서만 쓸 모델이라 여기에 정의, 복잡해지면 Domain으로 이동)
    data class TableItem(
        val id: String,         // 고유 ID
        val label: String,      // "4인 테이블"
        val capacityPerTable: Int, // 테이블 당 의자 수 (4 or 2)
        val maxTableCount: Int, // 가게에 있는 총 테이블 개수
        val currentCount: Int = 0 // 현재 사용 중인 테이블 개수
    )

    data class FloorCategory(
        val id: String,
        val name: String,
        val tableList: List<TableItem>
    )

    // 2. UI State
    data class SeatManagementUiState(
        val categories: List<FloorCategory> = emptyList(), // 전체, 1층, 2층
        val selectedCategoryId: String = "ALL",            // 현재 선택된 탭
        val displayItems: List<TableItem> = emptyList(),   // 화면에 보여줄 테이블 리스트
        val totalSeatCapacity: Int = 0,                    // 전체 좌석 수 (ex: 36석)
        val currentUsedSeats: Int = 0,                     // 현재 사용 중인 좌석 수
        val isSaving: Boolean = false
    )

    private val _uiState = MutableStateFlow(SeatManagementUiState())
    val uiState = _uiState.asStateFlow()

    // 초기화: Mock Data 로드
    init {
        loadMockData()
    }

    private fun loadMockData() {
        // 요구사항: 1층 (4인x2, 2인x4), 2층 (4인x4, 2인x2)
        val floor1Tables = listOf(
            TableItem("1F_4", "4인 테이블", 4, 2),
            TableItem("1F_2", "2인 테이블", 2, 4)
        )
        val floor2Tables = listOf(
            TableItem("2F_4", "4인 테이블", 4, 4),
            TableItem("2F_2", "2인 테이블", 2, 2)
        )

        // "전체"는 1층과 2층 데이터를 합쳐서 계산해야 함 (단순 리스트 합치기가 아니라 타입별 합산)
        // 로직 편의상 UI에서는 "전체" 탭일 때 1층, 2층 데이터를 모두 가지고 동적으로 합산하여 보여주는 방식을 택하거나
        // 여기서는 데이터 구조상 별도 리스트로 관리
        val allTables = listOf(
            TableItem("ALL_4", "4인 테이블", 4, 2 + 4), // 총 6개
            TableItem("ALL_2", "2인 테이블", 2, 4 + 2)  // 총 6개
        )

        val categories = listOf(
            FloorCategory("ALL", "전체", allTables),
            FloorCategory("1F", "1층", floor1Tables),
            FloorCategory("2F", "2층", floor2Tables) // 지하 1층 대신 2층으로 구현
        )

        _uiState.update {
            it.copy(
                categories = categories,
                selectedCategoryId = "ALL",
                displayItems = allTables // 초기값은 전체
            )
        }
        calculateSeatStats()
    }

    // 3. Action 처리
    fun onCategorySelected(categoryId: String) {
        val selectedCategory = _uiState.value.categories.find { it.id == categoryId } ?: return
        _uiState.update {
            it.copy(
                selectedCategoryId = categoryId,
                displayItems = selectedCategory.tableList
            )
        }
        // 탭이 바뀌어도 전체 통계(Occupied/Total)는 "전체 기준"으로 보여줄지, "해당 층 기준"으로 보여줄지 결정해야 함.
        // UI상 "빈 좌석 수 / 전체 좌석 수"는 보통 가게 전체를 의미하므로 유지하거나, 층별 필터링을 원하면 아래 calculate 호출
        calculateSeatStats() 
    }

    fun onIncrement(itemId: String) {
        updateTableCount(itemId, 1)
    }

    fun onDecrement(itemId: String) {
        updateTableCount(itemId, -1)
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
            
            // ★ 중요: 여기서 변경된 값을 원본 카테고리 데이터에도 반영해야 "전체 <-> 1층" 왔다갔다 해도 싱크가 맞음.
            // 하지만 지금은 Mocking 단계이므로 현재 보여지는 리스트만 수정하고 통계 재계산
            calculateSeatStats()
        }
    }

    // 좌석 수 계산 (테이블 수 * 인승)
    private fun calculateSeatStats() {
        val currentItems = _uiState.value.displayItems
        
        // 현재 화면에 보이는 아이템 기준으로 계산 (필터링된 뷰의 혼잡도)
        val totalCapacity = currentItems.sumOf { it.maxTableCount * it.capacityPerTable }
        val currentUsed = currentItems.sumOf { it.currentCount * it.capacityPerTable }

        _uiState.update {
            it.copy(
                totalSeatCapacity = totalCapacity,
                currentUsedSeats = currentUsed
            )
        }
    }

    fun onSave() {
        // API 연동 로직 (추후 구현)
        _uiState.update { it.copy(isSaving = true) }
        // ... Networking ...
    }
}