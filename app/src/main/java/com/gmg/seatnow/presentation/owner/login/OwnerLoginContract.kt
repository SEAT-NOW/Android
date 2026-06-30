package com.gmg.seatnow.presentation.owner.login

data class OwnerLoginUiState(
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val loginError: String? = null,
    val isLoading: Boolean = false,
    val isLoginButtonEnabled: Boolean = false
)

sealed class OwnerLoginAction {
    data class UpdateEmail(val email: String) : OwnerLoginAction()
    data class UpdatePassword(val password: String) : OwnerLoginAction()
    object OnLoginClick : OwnerLoginAction()
    object OnSignUpClick : OwnerLoginAction()
    object OnFindEmailClick : OwnerLoginAction()
    object OnFindPasswordClick : OwnerLoginAction()
}

sealed class OwnerLoginEvent {
    object NavigateToOwnerMain : OwnerLoginEvent()
    object NavigateToSignUp : OwnerLoginEvent()
    object NavigateToFindEmail : OwnerLoginEvent()
    object NavigateToFindPassword : OwnerLoginEvent()
}
