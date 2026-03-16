package com.gmg.seatnow.presentation.user.term

// 약관 타입 정의
enum class UserTermType(val title: String) {
    AGE("[필수] 만 14세 이상"),
    SERVICE("[필수] 이용약관 동의"),
    PRIVACY_COLLECT("[필수] 개인정보 수집이용 동의"),
    PRIVACY_PROVIDE("[필수] 개인정보 처리방침 동의"),
    LOCATION("[필수] 위치기반 서비스 이용약관 동의")
}

// UI 상태
data class UserTermsUiState(
    val isAllChecked: Boolean = false,
    val isAgeChecked: Boolean = false,
    val isServiceChecked: Boolean = false,
    val isPrivacyCollectChecked: Boolean = false,
    val isPrivacyProvideChecked: Boolean = false,
    val isLocationChecked: Boolean = false,
    val openedTermType: UserTermType? = null // 상세 화면이 열려있는지 여부
) {
    // 모두 체크되었는지 확인
    val isNextEnabled: Boolean
        get() = isAgeChecked && isServiceChecked && isPrivacyCollectChecked &&
                isPrivacyProvideChecked && isLocationChecked
}

sealed interface UserTermsEvent {
    object NavigateToMain : UserTermsEvent
}

sealed interface UserTermsAction {
    data class OnToggleAll(val isChecked: Boolean) : UserTermsAction
    data class OnToggleTerm(val type: UserTermType) : UserTermsAction
    data class OnOpenDetail(val type: UserTermType) : UserTermsAction
    object OnCloseDetail : UserTermsAction
    data class OnSaveTermsAgreement(val isGuest: Boolean) : UserTermsAction
}
