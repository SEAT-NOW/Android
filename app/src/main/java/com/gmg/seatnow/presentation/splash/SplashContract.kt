package com.gmg.seatnow.presentation.splash

data class SplashUiState(
    val isLoading: Boolean = true
)

sealed interface SplashAction {
    // Splash 화면에서는 초기 자동 로그인 체크만 진행하되 확장성을 위해 둠
}

sealed interface SplashEvent {
    object NavigateToUserMain : SplashEvent
    object NavigateToOwnerMain : SplashEvent
    object NavigateToLogin : SplashEvent
    data class NavigateToTerms(val isGuest: Boolean) : SplashEvent
}
