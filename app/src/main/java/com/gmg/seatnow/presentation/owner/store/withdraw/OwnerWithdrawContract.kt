package com.gmg.seatnow.presentation.owner.store.withdraw

// 1. UI State
data class OwnerWithdrawUiState(
    val isConfirmed: Boolean = false,
    val businessNumber: String = "", 
    val password: String = "",       
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val isButtonEnabled: Boolean
        get() = isConfirmed && businessNumber.isNotBlank() && password.isNotBlank() && !isLoading
}

// 2. Event
sealed interface OwnerWithdrawEvent {
    data object NavigateToLogin : OwnerWithdrawEvent
    data object PopBackStack : OwnerWithdrawEvent
}

// 3. Action
sealed interface OwnerWithdrawAction {
    data object OnToggleConfirm : OwnerWithdrawAction
    data class OnBusinessNumberChange(val number: String) : OwnerWithdrawAction
    data class OnPasswordChange(val password: String) : OwnerWithdrawAction
    data object OnWithdrawClick : OwnerWithdrawAction
    data object OnBackClick : OwnerWithdrawAction
}
