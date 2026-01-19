package com.gmg.seatnow.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.usecase.auth.AutoLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val autoLoginUseCase: AutoLoginUseCase
) : ViewModel() {

    private val _event = MutableSharedFlow<SplashEvent>()
    val event: SharedFlow<SplashEvent> = _event

    init {
        checkAutoLogin()
    }

    private fun checkAutoLogin() {
        viewModelScope.launch {
            // [병렬 처리] 1.5초 대기 & 서버 토큰 검증
            val delayJob = async { delay(1500) }
            val loginJob = async { autoLoginUseCase() } // 유효성 검사

            delayJob.await()
            val isSuccess = loginJob.await()

            if (isSuccess) {
                // ★ [수정] 토큰 유효 시 '사장님 메인'으로 이동하도록 변경
                _event.emit(SplashEvent.NavigateToOwnerMain)
            } else {
                _event.emit(SplashEvent.NavigateToLogin)
            }
        }
    }

    sealed class SplashEvent {
        object NavigateToLogin : SplashEvent()
        object NavigateToOwnerMain : SplashEvent() // ★ 이름 변경 (NavigateToMain -> NavigateToOwnerMain)
    }
}