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
            delay(1500) // 스플래시 지연 시간

            // API 호출 없이 로컬에 저장된 데이터만 확인합니다.
            val hasToken = authManager.hasToken()
            val isGuestAgreed = authManager.isGuestTermsAgreed()
            val storeId = authManager.getStoreId()

            // [핵심] 토큰이 존재하면 서버 검증 없이 바로 홈 화면으로 보냅니다.
            // (만약 만료된 토큰이라면, 나중에 홈 화면에서 API를 호출할 때 Interceptor가 알아서 재발급을 처리합니다.)
            if (hasToken) {
                if (storeId != -1L) {
                    _event.emit(SplashEvent.NavigateToOwnerMain)
                } else if (authManager.isKakaoTermsAgreed()) {
                    _event.emit(SplashEvent.NavigateToUserMain)
                } else {
                    _event.emit(SplashEvent.NavigateToTerms(isGuest = false))
                }
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