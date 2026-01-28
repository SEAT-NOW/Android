package com.gmg.seatnow.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.domain.usecase.auth.LoginWithKakaoUseCase
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginWithKakaoUseCase: LoginWithKakaoUseCase,
    private val authManager: AuthManager
) : ViewModel() {

    private val _event = MutableSharedFlow<LoginEvent>()
    val event: SharedFlow<LoginEvent> = _event

    fun onKakaoLoginClick() {
        viewModelScope.launch {
            // 1. 카카오 로그인 플로우 실행 (토큰 발급 + 백엔드 검증)
            loginWithKakaoUseCase().fold(
                onSuccess = {
                    // 백엔드 로그인이 성공하면, 카카오 SDK를 통해 닉네임과 이메일 정보를 가져옵니다.
                    fetchKakaoUserInfoAndNavigate()
                },
                onFailure = { error ->
                    error.printStackTrace()
                    // UI에 에러 메시지 띄우는 로직 추가 가능
                }
            )
        }
    }

    // [핵심] 닉네임과 이메일만 SDK에서 가져와서 AuthManager에 저장하는 함수
    private fun fetchKakaoUserInfoAndNavigate() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                error.printStackTrace()
                return@me
            }

            if (user != null) {
                // [수정] 카카오 프로필에서 '닉네임'만 추출
                val nickname = user.kakaoAccount?.profile?.nickname

                // [수정] AuthManager에 닉네임만 저장
                authManager.saveUserInfo(nickname = nickname)

                // 약관 동의 체크 후 화면 이동
                viewModelScope.launch {
                    if (authManager.isKakaoTermsAgreed()) {
                        _event.emit(LoginEvent.NavigateToUserMain)
                    } else {
                        _event.emit(LoginEvent.NavigateToTerms(isGuest = false))
                    }
                }
            }
        }
    }

    fun onOwnerLoginClick() {
        viewModelScope.launch {
            _event.emit(LoginEvent.NavigateToOwnerLogin)
        }
    }

    fun onGuestLoginClick() {
        viewModelScope.launch {
            if (authManager.isGuestTermsAgreed()) {
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