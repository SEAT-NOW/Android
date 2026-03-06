package com.gmg.seatnow.presentation.user.mypage

data class UserMyPageUiState(
    val nickname: String = "",
    val isGuest: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface UserMyPageEvent {
    data object NavigateToLogin : UserMyPageEvent // 게스트 & 일반유저 공용
    data object NavigateToAccountInfo : UserMyPageEvent
    data object NavigateToWithdraw : UserMyPageEvent
}

sealed interface UserMyPageAction {
    data object OnAccountInfoClick : UserMyPageAction
    data object OnLogoutClick : UserMyPageAction
    data object OnWithdrawClick : UserMyPageAction
}
