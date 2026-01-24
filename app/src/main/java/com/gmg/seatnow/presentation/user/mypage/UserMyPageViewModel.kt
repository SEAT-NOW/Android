package com.gmg.seatnow.presentation.user.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.domain.usecase.auth.OwnerLogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserMyPageViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val logoutUseCase: OwnerLogoutUseCase
) : ViewModel() {

    data class UserMyPageUiState(
        val nickname: String = "",
        val isGuest: Boolean = false,
        val isLoading: Boolean = false
    )

    private val _uiState = MutableStateFlow(UserMyPageUiState())
    val uiState = _uiState.asStateFlow()

    sealed interface UserMyPageEvent {
        data object NavigateToLogin : UserMyPageEvent // 게스트 & 일반유저 공용
        data object NavigateToAccountInfo : UserMyPageEvent
        data object NavigateToWithdraw : UserMyPageEvent
    }

    private val _event = MutableSharedFlow<UserMyPageEvent>()
    val event = _event.asSharedFlow()

    init {
        val isGuestUser = authManager.getAccessToken().isNullOrBlank()
        _uiState.update {
            it.copy(
                nickname = if (isGuestUser) "게스트" else (authManager.getUserNickname() ?: "사용자"),
                isGuest = isGuestUser
            )
        }
    }

    fun onAction(action: UserMyPageAction) {
        when (action) {
            is UserMyPageAction.OnAccountInfoClick -> {
                viewModelScope.launch { _event.emit(UserMyPageEvent.NavigateToAccountInfo) }
            }
            is UserMyPageAction.OnLogoutClick -> logout()
            is UserMyPageAction.OnWithdrawClick -> {
                viewModelScope.launch { _event.emit(UserMyPageEvent.NavigateToWithdraw) }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            // [수정] 게스트인 경우: API 호출 없이 바로 로그인 화면으로 이동 이벤트 발생
            if (uiState.value.isGuest) {
                _event.emit(UserMyPageEvent.NavigateToLogin)
                return@launch
            }

            // 일반 회원인 경우: 기존 로그아웃 API 호출
            _uiState.update { it.copy(isLoading = true) }
            logoutUseCase()
                .onSuccess {
                    authManager.clearTokens()
                    _event.emit(UserMyPageEvent.NavigateToLogin)
                }
                .onFailure { _event.emit(UserMyPageEvent.NavigateToLogin) }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

sealed interface UserMyPageAction {
    data object OnAccountInfoClick : UserMyPageAction
    data object OnLogoutClick : UserMyPageAction
    data object OnWithdrawClick : UserMyPageAction
}