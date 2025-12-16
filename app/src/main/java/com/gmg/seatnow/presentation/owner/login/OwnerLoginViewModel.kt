package com.gmg.seatnow.presentation.owner.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.usecase.OwnerLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerLoginViewModel @Inject constructor(
    private val ownerLoginUseCase: OwnerLoginUseCase // UseCase 주입 받음
) : ViewModel() {

    // 1. 입력 값
    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    // 2. 입력 필드 에러 메시지 (실시간 검증용)
    private val _emailError = MutableStateFlow<String?>(null)
    val emailError = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError = _passwordError.asStateFlow()

    // 3. 로그인 실패 메시지 & 로딩 상태
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError = _loginError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // 4. 버튼 활성화 조건
    val isLoginButtonEnabled: StateFlow<Boolean> = combine(_email, _password, _emailError, _passwordError) { e, p, eErr, pErr ->
        e.isNotBlank() && p.isNotBlank() && eErr == null && pErr == null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _event = MutableSharedFlow<OwnerLoginEvent>()
    val event = _event.asSharedFlow()

    // 정규식 정의 (ViewModel UI State 검증용이므로 여기에 위치)
    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$".toRegex()
    private val passwordRegex = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!@#\$%^&*()_+=-]).{8,20}\$".toRegex()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _loginError.value = null

        if (newEmail.isNotBlank() && !newEmail.matches(emailRegex)) {
            _emailError.value = "올바른 이메일 형식이 아닙니다."
        } else {
            _emailError.value = null
        }
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _loginError.value = null

        if (newPassword.isNotBlank() && !newPassword.matches(passwordRegex)) {
            _passwordError.value = "영문, 숫자, 특수문자 포함 8~20자리여야 합니다."
        } else {
            _passwordError.value = null
        }
    }

    // ★ UseCase를 통한 로그인 처리
    fun onLoginClick() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true

            // ViewModel은 "어떻게" 로그인하는지 모름. UseCase에게 "로그인 해줘"라고 명령만 함.
            val result = ownerLoginUseCase(
                email = _email.value,
                password = _password.value
            )

            result.onSuccess {
                _loginError.value = null
                _event.emit(OwnerLoginEvent.NavigateToOwnerMain)
            }.onFailure { exception ->
                // UseCase/Repository에서 넘겨준 에러 메시지 표시
                _loginError.value = exception.message ?: "로그인에 실패했습니다."
            }

            _isLoading.value = false
        }
    }

    fun onSignUpClick() {
        viewModelScope.launch {
            _event.emit(OwnerLoginEvent.NavigateToSignUp)
        }
    }

    sealed class OwnerLoginEvent {
        object NavigateToOwnerMain : OwnerLoginEvent()
        object NavigateToSignUp : OwnerLoginEvent()
    }
}