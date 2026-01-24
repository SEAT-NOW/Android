package com.gmg.seatnow.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.domain.usecase.auth.ReissueTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val reissueTokenUseCase: ReissueTokenUseCase
) : ViewModel() {

    private val _event = MutableSharedFlow<SplashEvent>()
    val event: SharedFlow<SplashEvent> = _event

    init {
        checkAutoLogin()
    }

    private fun checkAutoLogin() {
        viewModelScope.launch {
            delay(1500)

            val hasToken = authManager.hasToken()
            val isGuestAgreed = authManager.isGuestTermsAgreed()
            val storeId = authManager.getStoreId() // [핵심] 사장님 store_id 가져오기

            if (hasToken) {
                // 토큰이 있다면 서버에 재발급(검증) 요청
                reissueTokenUseCase().fold(
                    onSuccess = {
                        // ★ 분기점: storeId가 -1L이 아니면 '사장님'이므로 사장님 메인으로 이동
                        if (storeId != -1L) {
                            _event.emit(SplashEvent.NavigateToOwnerMain)
                        }
                        // storeId가 없으면 '일반 유저' (카카오 약관 동의 확인)
                        else if (authManager.isKakaoTermsAgreed()) {
                            _event.emit(SplashEvent.NavigateToUserMain)
                        } else {
                            _event.emit(SplashEvent.NavigateToTerms(isGuest = false))
                        }
                    },
                    onFailure = {
                        _event.emit(SplashEvent.NavigateToLogin)
                    }
                )
            } else if (isGuestAgreed) {
                _event.emit(SplashEvent.NavigateToUserMain)
            } else {
                _event.emit(SplashEvent.NavigateToLogin)
            }
        }
    }

    sealed class SplashEvent {
        object NavigateToUserMain : SplashEvent()
        object NavigateToOwnerMain : SplashEvent() // [추가] 사장님 메인 이벤트
        object NavigateToLogin : SplashEvent()
        data class NavigateToTerms(val isGuest: Boolean) : SplashEvent()
    }
}