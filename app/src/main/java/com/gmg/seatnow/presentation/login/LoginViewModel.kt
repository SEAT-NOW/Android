package com.gmg.seatnow.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: AuthManager
) : ViewModel() {

    private val _event = MutableSharedFlow<LoginEvent>()
    val event: SharedFlow<LoginEvent> = _event

    fun onOwnerLoginClick() {
        viewModelScope.launch {
            _event.emit(LoginEvent.NavigateToOwnerLogin)
        }
    }

    fun onKakaoLoginClick() {
        viewModelScope.launch {
            authRepository.loginKakao()
                .onSuccess { token ->
                    tokenManager.saveAccessToken(token)

                    // ★ [수정] 카카오 전용 동의 여부 확인
                    if (tokenManager.isKakaoTermsAgreed()) {
                        _event.emit(LoginEvent.NavigateToUserMain)
                    } else {
                        // 카카오로 로그인했지만 약관 동의 안 함 -> 약관 화면으로
                        _event.emit(LoginEvent.NavigateToTerms(isGuest = false))
                    }
                }
                .onFailure { error ->
                    error.printStackTrace()
                }
        }
    }

    fun onGuestLoginClick() {
        viewModelScope.launch {
            // ★ [수정] 게스트 전용 동의 여부 확인
            if (tokenManager.isGuestTermsAgreed()) {
                _event.emit(LoginEvent.NavigateToUserMain)
            } else {
                _event.emit(LoginEvent.NavigateToTerms(isGuest = true))
            }
        }
    }

    sealed class LoginEvent {
        object NavigateToUserMain : LoginEvent()
        object NavigateToOwnerLogin : LoginEvent()
        data class NavigateToTerms(val isGuest: Boolean) : LoginEvent()
    }
}