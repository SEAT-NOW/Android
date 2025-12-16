package com.gmg.seatnow.presentation.owner.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerLoginViewModel @Inject constructor() : ViewModel() {

    // 1. 입력 값
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")

    // 2. 에러 메시지 상태 (null이면 에러 없음, 문자열이면 에러 있음)
    private val _emailError = MutableStateFlow<String?>(null)
    val emailError = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError = _passwordError.asStateFlow()

    // 3. 버튼 활성화 (입력값이 비어있지만 않으면 일단 활성화 -> 누르면 검증)
    val isLoginButtonEnabled: StateFlow<Boolean> = combine(email, password) { e, p ->
        e.isNotBlank() && p.isNotBlank()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _event = MutableSharedFlow<OwnerLoginEvent>()
    val event = _event.asSharedFlow()

    fun onEmailChange(newEmail: String) {
        email.value = newEmail
        _emailError.value = null // 타이핑 시작하면 에러 메시지 지워주기 (센스!)
    }

    fun onPasswordChange(newPassword: String) {
        password.value = newPassword
        _passwordError.value = null // 타이핑 시작하면 에러 메시지 지워주기
    }

    fun onLoginClick() {
        // [검증 로직 시작]
        val currentEmail = email.value
        val currentPassword = password.value
        var hasError = false

        // 이메일 검증 (간단하게 '@' 포함 여부만 체크, 필요시 정규식 사용)
        if (!currentEmail.contains("@")) {
            _emailError.value = "올바른 이메일 형식이 아닙니다."
            hasError = true
        }

        // 비밀번호 검증 (8자리 미만이면 에러)
        if (currentPassword.length < 8) {
            _passwordError.value = "비밀번호는 8자리 이상이어야 합니다."
            hasError = true
        }

        if (hasError) return // 에러 있으면 여기서 중단 (로그인 시도 X)

        // [모든 검증 통과 -> 실제 로그인 시도]
        viewModelScope.launch {
            _event.emit(OwnerLoginEvent.NavigateToOwnerMain)
        }
    }

    sealed class OwnerLoginEvent {
        object NavigateToOwnerMain : OwnerLoginEvent()
    }
}