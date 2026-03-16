package com.gmg.seatnow.presentation.owner.store

data class StoreMainUiState(
    val currentTab: StoreTab = StoreTab.SEAT_MANAGEMENT
)

sealed interface StoreMainAction {
    data class ChangeTab(val tab: StoreTab) : StoreMainAction
}

sealed interface StoreMainEvent {
}
