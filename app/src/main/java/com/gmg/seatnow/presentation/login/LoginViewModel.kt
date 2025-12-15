package com.gmg.seatnow.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _event = MutableSharedFlow<LoginEvent>()
    val event: SharedFlow<LoginEvent> = _event

    fun onKakaoLoginClick() {
        viewModelScope.launch {
            authRepository.loginKakao()
                .onSuccess { token ->
                    _event.emit(LoginEvent.NavigateToUserMain)
                }
                .onFailure { error ->
                    error.printStackTrace()
                }
        }
    }

    fun onOwnerLoginClick() {
        viewModelScope.launch {
            _event.emit(LoginEvent.NavigateToOwnerLogin)
        }
    }

    sealed class LoginEvent {
        object NavigateToUserMain : LoginEvent()
        object NavigateToOwnerLogin : LoginEvent()
    }
}