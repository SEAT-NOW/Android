package com.gmg.seatnow.presentation.owner.store.seat

import com.gmg.seatnow.domain.model.FloorCategory
import com.gmg.seatnow.domain.model.TableItem

enum class SeatDisplayMode {
    EMPTY,
    OCCUPIED
}

data class SeatManagementUiState(
    val loadState: LoadState = LoadState(),
    val displayState: DisplayState = DisplayState(),
    val seatData: SeatDataState = SeatDataState()
) {
    data class LoadState(
        val isLoading: Boolean = false,
        val isSaving: Boolean = false
    )
    
    data class DisplayState(
        val selectedCategoryId: String = "ALL",
        val displayMode: SeatDisplayMode = SeatDisplayMode.EMPTY,
        val isEditMode: Boolean = false
    )
    
    data class SeatDataState(
        val categories: List<FloorCategory> = emptyList(),
        val groupedDisplayItems: Map<String, List<TableItem>> = emptyMap(),
        val totalSeatCapacity: Int = 0,
        val currentUsedSeats: Int = 0
    )
}

sealed interface SeatManagementEvent {
    data class ShowToast(val message: String) : SeatManagementEvent
}

sealed interface SeatManagementAction {
    data class SelectCategory(val categoryId: String) : SeatManagementAction
    data class IncrementTableCount(val itemId: String) : SeatManagementAction
    data class DecrementTableCount(val itemId: String) : SeatManagementAction
    data class ToggleDisplayMode(val mode: SeatDisplayMode) : SeatManagementAction
    data object OnUpdateClick : SeatManagementAction
    data object OnSaveClick : SeatManagementAction
}
