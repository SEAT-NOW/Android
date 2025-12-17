package com.gmg.seatnow.presentation.owner.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerSignUpViewModel @Inject constructor() : ViewModel() {

    // 1. UI 상태 (입력값 + 현재 단계)
    private val _uiState = MutableStateFlow(OwnerSignUpUiState())
    val uiState: StateFlow<OwnerSignUpUiState> = _uiState.asStateFlow()

    // 2. 단발성 이벤트 (화면 이동 등)
    private val _event = MutableSharedFlow<SignUpEvent>()
    val event: SharedFlow<SignUpEvent> = _event.asSharedFlow()

    // 정규식 정의
    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$".toRegex()
    private val passwordRegex = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!@#\$%^&*()_+=-]).{8,20}\$".toRegex()

    // 3. 사용자 동작 처리
    fun onAction(action: SignUpAction) {
        when (action) {
            is SignUpAction.UpdateEmail -> validateAndUpdateEmail(action.email)
            is SignUpAction.UpdateAuthCode -> _uiState.update { it.copy(authCode = action.code) }
            is SignUpAction.UpdatePassword -> validateAndUpdatePassword(action.password)
            is SignUpAction.UpdatePasswordCheck -> validateAndUpdatePasswordCheck(action.check)
            is SignUpAction.UpdatePhone -> _uiState.update { it.copy(phone = action.phone) }

            is SignUpAction.OnNextClick -> handleNextStep()
            is SignUpAction.OnBackClick -> handleBackStep()
        }
    }

    // 이메일 검증
    private fun validateAndUpdateEmail(email: String) {
        val error = if (email.isNotBlank() && !email.matches(emailRegex)) {
            "올바른 이메일 형식이 아닙니다."
        } else {
            null
        }
        _uiState.update { it.copy(email = email, emailError = error) }
    }

    // 비밀번호 검증
    private fun validateAndUpdatePassword(password: String) {
        val error = if (password.isNotBlank() && !password.matches(passwordRegex)) {
            "영문, 숫자, 특수문자 포함 8~20자리여야 합니다."
        } else {
            null
        }

        _uiState.update {
            // 비밀번호가 바뀌면 비밀번호 확인란도 다시 검사해야 함
            val checkError = if (it.passwordCheck.isNotBlank() && it.passwordCheck != password) {
                "비밀번호가 일치하지 않습니다."
            } else {
                null
            }
            it.copy(password = password, passwordError = error, passwordCheckError = checkError)
        }
    }

    // 비밀번호 확인 검증
    private fun validateAndUpdatePasswordCheck(check: String) {
        val currentPassword = _uiState.value.password
        val error = if (check.isNotBlank() && check != currentPassword) {
            "비밀번호가 일치하지 않습니다."
        } else {
            null
        }
        _uiState.update { it.copy(passwordCheck = check, passwordCheckError = error) }
    }

    private fun handleNextStep() {
        val currentStep = _uiState.value.currentStep
        val nextOrdinal = currentStep.ordinal + 1

        if (nextOrdinal < SignUpStep.entries.size) {
            // 다음 단계로 이동
            val nextStep = SignUpStep.entries[nextOrdinal]
            _uiState.update { it.copy(currentStep = nextStep) }
        } else {
            // 마지막 단계(완료)라면 홈으로 이동 이벤트 발생
            viewModelScope.launch {
                _event.emit(SignUpEvent.NavigateToHome)
            }
        }
    }

    private fun handleBackStep() {
        val currentStep = _uiState.value.currentStep
        val prevOrdinal = currentStep.ordinal - 1

        if (prevOrdinal >= 0) {
            // 이전 단계로 이동
            _uiState.update { it.copy(currentStep = SignUpStep.entries[prevOrdinal]) }
        } else {
            // 첫 단계에서 뒤로가기 시 화면 종료 이벤트 발생
            viewModelScope.launch {
                _event.emit(SignUpEvent.NavigateBack)
            }
        }
    }
}

// --- 관련 데이터 클래스들 (같은 파일 하단 혹은 별도 파일) ---

data class OwnerSignUpUiState(
    val currentStep: SignUpStep = SignUpStep.STEP_1_BASIC,
    val email: String = "",
    val emailError: String? = null, // 에러 상태
    val authCode: String = "",
    val password: String = "",
    val passwordError: String? = null, // 에러 상태
    val passwordCheck: String = "",
    val passwordCheckError: String? = null, // 에러 상태
    val phone: String = "",
    val businessNumber: String = "",
    val storeName: String = ""
)

sealed interface SignUpAction {
    data class UpdateEmail(val email: String) : SignUpAction
    data class UpdateAuthCode(val code: String) : SignUpAction
    data class UpdatePassword(val password: String) : SignUpAction
    data class UpdatePasswordCheck(val check: String) : SignUpAction
    data class UpdatePhone(val phone: String) : SignUpAction

    object OnNextClick : SignUpAction
    object OnBackClick : SignUpAction
}

sealed interface SignUpEvent {
    object NavigateBack : SignUpEvent
    object NavigateToHome : SignUpEvent
}