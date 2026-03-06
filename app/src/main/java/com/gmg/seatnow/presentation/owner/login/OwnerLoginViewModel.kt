package com.gmg.seatnow.presentation.owner.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.usecase.auth.OwnerLoginUseCase
import com.gmg.seatnow.domain.usecase.logic.ValidateEmailUseCase
import com.gmg.seatnow.domain.usecase.logic.ValidatePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerLoginViewModel @Inject constructor(
    private val ownerLoginUseCase: OwnerLoginUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerLoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<OwnerLoginEvent>()
    val event = _event.asSharedFlow()

    fun onAction(action: OwnerLoginAction) {
        when (action) {
            is OwnerLoginAction.UpdateEmail -> updateEmail(action.email)
            is OwnerLoginAction.UpdatePassword -> updatePassword(action.password)
            is OwnerLoginAction.OnLoginClick -> performLogin()
            is OwnerLoginAction.OnSignUpClick -> {
                viewModelScope.launch { _event.emit(OwnerLoginEvent.NavigateToSignUp) }
            }
        }
    }

    private fun updateEmail(email: String) {
        val error = validateEmailUseCase(email)
        _uiState.update { it.copy(email = email, emailError = error, loginError = null) }
        checkButtonEnabled()
    }

    private fun updatePassword(password: String) {
        val error = validatePasswordUseCase(password)
        _uiState.update { it.copy(password = password, passwordError = error, loginError = null) }
        checkButtonEnabled()
    }

    private fun checkButtonEnabled() {
        _uiState.update { state ->
            val isEnabled = state.email.isNotBlank() && state.password.isNotBlank() && 
                            state.emailError == null && state.passwordError == null
            state.copy(isLoginButtonEnabled = isEnabled)
        }
    }

    private fun performLogin() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = ownerLoginUseCase(
                email = _uiState.value.email,
                password = _uiState.value.password
            )

            result.onSuccess {
                _uiState.update { it.copy(loginError = null, isLoading = false) }
                _event.emit(OwnerLoginEvent.NavigateToOwnerMain)
            }.onFailure { exception ->
                _uiState.update { it.copy(loginError = exception.message ?: "로그인에 실패했습니다.", isLoading = false) }
            }
        }
    }
}