package com.gmg.seatnow.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.data.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _event = MutableSharedFlow<SplashEvent>()
    val event: SharedFlow<SplashEvent> = _event

    init {
        checkAutoLogin()
    }

    private fun checkAutoLogin() {
        viewModelScope.launch {
            // 스플래시 화면 유지 시간 (1.5초)
            val splashDelay = launch { delay(1500) }
            
            // 내부 저장소에서 토큰 꺼내기
            val savedToken = tokenManager.accessToken.first()

            splashDelay.join() // 딜레이 끝날 때까지 대기

            _event.emit(SplashEvent.NavigateToLogin) // 개발을 위한 자동 로그인 임시 방지 로직

            // 실질적인 자동 로그인 로직
//            if (savedToken.isNullOrBlank()) {
//                // 1. 저장된 토큰 없음 -> 로그인 화면으로
//                _event.emit(SplashEvent.NavigateToLogin)
//            } else {
//                // 2. 토큰 있음 -> 카카오 서버에 유효성 검사
//                UserApiClient.instance.accessTokenInfo { _, error ->
//                    viewModelScope.launch {
//                        if (error != null) {
//                            // 토큰 만료됨 -> 로그인 화면으로
//                            _event.emit(SplashEvent.NavigateToLogin)
//                        } else {
//                            // 토큰 유효함 -> 메인 화면으로 자동 이동 (🚀 자동 로그인 성공)
//                            _event.emit(SplashEvent.NavigateToUserMain)
//                        }
//                    }
//                }
//            }
        }
    }

    sealed class SplashEvent {
        object NavigateToLogin : SplashEvent()
        object NavigateToUserMain : SplashEvent()
    }
}