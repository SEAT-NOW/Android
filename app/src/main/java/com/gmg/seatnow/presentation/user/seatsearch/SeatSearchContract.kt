package com.gmg.seatnow.presentation.user.seatsearch

data class SeatSearchUiState(
    val headCount: String = "4",
    val isLoading: Boolean = false
)

sealed interface SeatSearchEvent {
    data class OnSearchConfirmed(val count: Int) : SeatSearchEvent
}

sealed interface SeatSearchAction {
    data class OnHeadCountChanged(val count: String) : SeatSearchAction
    data object OnHeadCountFinalize : SeatSearchAction
    data class OnAdjustHeadCount(val amount: Int) : SeatSearchAction
    data object OnSearchClick : SeatSearchAction
}
