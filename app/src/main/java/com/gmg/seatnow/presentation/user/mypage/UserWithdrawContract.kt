package com.gmg.seatnow.presentation.user.mypage

data class UserWithdrawUiState(
    val isConfirmed: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface UserWithdrawEvent {
    data object NavigateToLogin : UserWithdrawEvent
    data object PopBackStack : UserWithdrawEvent
    data class ShowToast(val message: String) : UserWithdrawEvent
}

sealed interface UserWithdrawAction {
    data object OnToggleConfirm : UserWithdrawAction
    data object OnWithdrawClick : UserWithdrawAction
    data object OnBackClick : UserWithdrawAction
}
