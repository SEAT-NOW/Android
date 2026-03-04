package com.gmg.seatnow.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.domain.model.SplashDestination
import com.gmg.seatnow.domain.usecase.auth.GetSplashDestinationUseCase
import com.gmg.seatnow.domain.usecase.auth.ReissueTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getSplashDestinationUseCase: GetSplashDestinationUseCase,
) : ViewModel() {

    private val _event = MutableSharedFlow<SplashEvent>()
    val event: SharedFlow<SplashEvent> = _event

    init {
        checkAutoLogin()
    }

    private fun checkAutoLogin() {
        viewModelScope.launch {
            delay(1500) // 스플래시 지연 시간

            when (getSplashDestinationUseCase()) {
                SplashDestination.OWNER_MAIN -> _event.emit(SplashEvent.NavigateToOwnerMain)
                SplashDestination.USER_MAIN -> _event.emit(SplashEvent.NavigateToUserMain)
                SplashDestination.TERMS_KAKAO -> _event.emit(SplashEvent.NavigateToTerms(isGuest = false))
                SplashDestination.TERMS_GUEST -> _event.emit(SplashEvent.NavigateToTerms(isGuest = true))
                SplashDestination.LOGIN -> _event.emit(SplashEvent.NavigateToLogin)
            }
        }
    }
}