package com.gmg.seatnow.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.data.local.AppConfigManager
import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.data.local.UserManager
import com.gmg.seatnow.domain.usecase.user.auth.CheckGuestTermsUseCase
import com.gmg.seatnow.domain.usecase.user.auth.CheckKakaoTermsUseCase
import com.gmg.seatnow.domain.usecase.user.auth.LoginWithKakaoUseCase
import com.gmg.seatnow.domain.usecase.user.auth.SaveKakaoUserInfoUseCase
import com.gmg.seatnow.domain.usecase.user.auth.SetDeveloperModeUseCase
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginWithKakaoUseCase: LoginWithKakaoUseCase,
    private val saveKakaoUserInfoUseCase: SaveKakaoUserInfoUseCase,
    private val checkKakaoTermsUseCase: CheckKakaoTermsUseCase,
    private val checkGuestTermsUseCase: CheckGuestTermsUseCase,
    private val setDeveloperModeUseCase: SetDeveloperModeUseCase
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

                // [수정] userManager에 닉네임만 저장
                saveKakaoUserInfoUseCase(nickname)

                viewModelScope.launch {
                    // ★ UseCase를 통해 약관 동의 여부 확인
                    if (checkKakaoTermsUseCase()) {
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
            if (checkGuestTermsUseCase()) {
                _event.emit(LoginEvent.NavigateToUserMain)
            } else {
                _event.emit(LoginEvent.NavigateToTerms(isGuest = true))
            }
        }
    }

    fun onDeveloperLoginSuccess() {
        setDeveloperModeUseCase()
    }

    fun onDeveloperLoginClick() {
        viewModelScope.launch {
            _event.emit(LoginEvent.NavigateToDeveloperLogin)
        }
    }

    fun verifyDeveloperCode(code: String) {
        viewModelScope.launch {
            if (code == "seatnow!!testID") {
                // 성공 시 UseCase 실행 후 결과 이벤트 방출
                setDeveloperModeUseCase()
                _event.emit(LoginEvent.ShowToast("개발자 모드로 진입합니다."))
                _event.emit(LoginEvent.NavigateToUserMain)
            } else {
                // 실패 시 실패 이벤트 방출
                _event.emit(LoginEvent.ShowToast("코드가 올바르지 않습니다."))
            }
        }
    }

    sealed class LoginEvent {
        object NavigateToUserMain : LoginEvent()
        object NavigateToOwnerLogin : LoginEvent()
        object NavigateToDeveloperLogin : LoginEvent()
        data class NavigateToTerms(val isGuest: Boolean) : LoginEvent()
        data class ShowToast(val message: String) : LoginEvent()
    }
}