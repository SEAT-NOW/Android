package com.gmg.seatnow.presentation.owner.login

data class OwnerFindEmailUiState(
    val phone: String = "",
    val phoneError: String? = null,
    val isPhoneCodeSent: Boolean = false,
    val isPhoneVerified: Boolean = false,
    val isPhoneVerificationAttempted: Boolean = false,
    val phoneVerifiedError: String? = null,
    val phoneTimerText: String? = null,
    val isPhoneTimerExpired: Boolean = false,
    val phoneAuthCode: String = "",
    val isNextButtonEnabled: Boolean = false
)

sealed interface OwnerFindEmailAction {
    data class UpdatePhone(val phone: String) : OwnerFindEmailAction
    data class UpdatePhoneAuthCode(val code: String) : OwnerFindEmailAction
    object RequestPhoneCode : OwnerFindEmailAction
    object VerifyPhoneCode : OwnerFindEmailAction
    object OnNextClick : OwnerFindEmailAction
}

sealed interface OwnerFindEmailEvent {
    object NavigateBack : OwnerFindEmailEvent
    data class ShowToast(val message: String) : OwnerFindEmailEvent
}
