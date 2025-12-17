package com.gmg.seatnow.presentation.owner.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.usecase.OwnerAuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerSignUpViewModel @Inject constructor(
    private val authUseCase: OwnerAuthUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerSignUpUiState())
    val uiState: StateFlow<OwnerSignUpUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<SignUpEvent>()
    val event: SharedFlow<SignUpEvent> = _event.asSharedFlow()

    private var emailTimerJob: Job? = null
    private var phoneTimerJob: Job? = null

    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$".toRegex()
    private val passwordRegex = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!@#\$%^&*()_+=-]).{8,20}\$".toRegex()

    fun onAction(action: SignUpAction) {
        when (action) {
            is SignUpAction.UpdateEmail -> validateAndUpdateEmail(action.email)
            is SignUpAction.UpdateAuthCode -> _uiState.update { it.copy(authCode = action.code) }
            is SignUpAction.UpdatePassword -> validateAndUpdatePassword(action.password)
            is SignUpAction.UpdatePasswordCheck -> validateAndUpdatePasswordCheck(action.check)
            is SignUpAction.UpdatePhone -> _uiState.update { it.copy(phone = action.phone) }
            is SignUpAction.UpdatePhoneAuthCode -> _uiState.update { it.copy(phoneAuthCode = action.code) }

            is SignUpAction.RequestEmailCode -> requestEmailCode()
            is SignUpAction.VerifyEmailCode -> verifyEmailCode()
            is SignUpAction.RequestPhoneCode -> requestPhoneCode()
            is SignUpAction.VerifyPhoneCode -> verifyPhoneCode()

            is SignUpAction.OnNextClick -> handleNextStep()
            is SignUpAction.OnBackClick -> handleBackStep()
        }
        checkNextButtonEnabled()
    }

    // --- 이메일 인증 로직 ---
    private fun requestEmailCode() {
        val email = _uiState.value.email
        if (email.isBlank() || _uiState.value.emailError != null) return

        viewModelScope.launch {
            authUseCase.requestAuthCode(email)
                .onSuccess {
                    startEmailTimer()
                    // ★ 인증번호 재요청 시: '시도 여부(isEmailVerificationAttempted)'를 다시 false로 초기화하여 입력 가능하게 함
                    _uiState.update {
                        it.copy(
                            isEmailCodeSent = true,
                            authCode = "",
                            isEmailVerificationAttempted = false // 초기화
                        )
                    }
                }
                .onFailure { /* 에러 처리 */ }
        }
    }

    private fun verifyEmailCode() {
        val email = _uiState.value.email
        val code = _uiState.value.authCode

        // ★ 확인 버튼 누르는 순간: 즉시 텍스트 필드를 비활성화하기 위해 상태 업데이트
        _uiState.update { it.copy(isEmailVerificationAttempted = true) }

        // ★ 확인 버튼 누르면 타이머 정지 (성공/실패 상관없이 멈추기를 원하실 경우)
        stopEmailTimer()

        viewModelScope.launch {
            authUseCase.verifyAuthCode(email, code)
                .onSuccess {
                    _uiState.update { it.copy(isEmailVerified = true, emailTimerText = null) }
                    checkNextButtonEnabled()
                }
                .onFailure {
                    // 실패해도 isEmailVerificationAttempted가 true이므로 입력창은 비활성화 상태 유지됨
                    // (재입력을 원하면 '재전송'을 눌러야 함)
                }
        }
    }

    private fun startEmailTimer() {
        emailTimerJob?.cancel()
        emailTimerJob = viewModelScope.launch {
            var time = 180 // 3분
            _uiState.update { it.copy(isEmailTimerExpired = false) }

            // ★ Job이 취소되면(stopEmailTimer 호출 시) 루프도 자동으로 종료되므로 time > 0만 확인하면 됨
            while (time > 0) {
                val minutes = time / 60
                val seconds = time % 60
                val timeString = "%d:%02d".format(minutes, seconds)
                _uiState.update { it.copy(emailTimerText = timeString) }
                delay(1000)
                time--
            }
            _uiState.update { it.copy(emailTimerText = "0:00", isEmailTimerExpired = true) }
        }
    }

    private fun stopEmailTimer() {
        // ★ Job을 취소하면 코루틴 내부의 while 루프도 즉시 멈춥니다.
        emailTimerJob?.cancel()
        _uiState.update { it.copy(emailTimerText = null) }
    }

    // --- 휴대폰 인증 로직 ---
    private fun requestPhoneCode() {
        val phone = _uiState.value.phone
        if (phone.length < 10) return

        viewModelScope.launch {
            authUseCase.requestAuthCode(phone)
                .onSuccess {
                    startPhoneTimer()
                    // ★ 재전송 시 '시도 여부' 초기화
                    _uiState.update {
                        it.copy(
                            isPhoneCodeSent = true,
                            phoneAuthCode = "",
                            isPhoneVerificationAttempted = false
                        )
                    }
                }
        }
    }

    private fun verifyPhoneCode() {
        val phone = _uiState.value.phone
        val code = _uiState.value.phoneAuthCode

        // ★ 확인 버튼 누르는 순간 비활성화 처리
        _uiState.update { it.copy(isPhoneVerificationAttempted = true) }
        stopPhoneTimer()

        viewModelScope.launch {
            authUseCase.verifyAuthCode(phone, code)
                .onSuccess {
                    _uiState.update { it.copy(isPhoneVerified = true, phoneTimerText = null) }
                    checkNextButtonEnabled()
                }
        }
    }

    private fun startPhoneTimer() {
        phoneTimerJob?.cancel()
        phoneTimerJob = viewModelScope.launch {
            var time = 180
            _uiState.update { it.copy(isPhoneTimerExpired = false) }
            while (time > 0) {
                val minutes = time / 60
                val seconds = time % 60
                val timeString = "%d:%02d".format(minutes, seconds)
                _uiState.update { it.copy(phoneTimerText = timeString) }
                delay(1000)
                time--
            }
            _uiState.update { it.copy(phoneTimerText = "0:00", isPhoneTimerExpired = true) }
        }
    }

    private fun stopPhoneTimer() {
        phoneTimerJob?.cancel()
        _uiState.update { it.copy(phoneTimerText = null) }
    }

    // --- 유효성 검사 및 상태 업데이트 ---
    private fun validateAndUpdateEmail(email: String) {
        val error = if (email.isNotBlank() && !email.matches(emailRegex)) "올바른 이메일 형식이 아닙니다." else null
        // 이메일 수정 시 인증 관련 상태 모두 초기화
        _uiState.update {
            it.copy(
                email = email,
                emailError = error,
                isEmailVerified = false,
                isEmailCodeSent = false,
                isEmailVerificationAttempted = false // 초기화
            )
        }
        stopEmailTimer()
    }

    private fun validateAndUpdatePassword(password: String) {
        val error = if (password.isNotBlank() && !password.matches(passwordRegex)) "영문, 숫자, 특수문자 포함 8~20자리여야 합니다." else null
        _uiState.update { it.copy(password = password, passwordError = error) }
        validateAndUpdatePasswordCheck(_uiState.value.passwordCheck)
    }

    private fun validateAndUpdatePasswordCheck(check: String) {
        val currentPassword = _uiState.value.password
        val error = if (check.isNotBlank() && check != currentPassword) "비밀번호가 일치하지 않습니다." else null
        _uiState.update { it.copy(passwordCheck = check, passwordCheckError = error) }
    }

    private fun checkNextButtonEnabled() {
        val s = _uiState.value
        val isStep1Valid = s.isEmailVerified &&
                s.isPhoneVerified &&
                s.password.isNotBlank() && s.passwordError == null &&
                s.passwordCheck.isNotBlank() && s.passwordCheckError == null

        _uiState.update { it.copy(isNextButtonEnabled = isStep1Valid) }
    }

    private fun handleNextStep() {
        val currentStep = _uiState.value.currentStep
        val nextOrdinal = currentStep.ordinal + 1
        if (nextOrdinal < SignUpStep.entries.size) {
            _uiState.update { it.copy(currentStep = SignUpStep.entries[nextOrdinal]) }
        } else {
            viewModelScope.launch { _event.emit(SignUpEvent.NavigateToHome) }
        }
    }

    private fun handleBackStep() {
        val currentStep = _uiState.value.currentStep
        val prevOrdinal = currentStep.ordinal - 1
        if (prevOrdinal >= 0) {
            _uiState.update { it.copy(currentStep = SignUpStep.entries[prevOrdinal]) }
        } else {
            viewModelScope.launch { _event.emit(SignUpEvent.NavigateBack) }
        }
    }
}

// [UiState 업데이트] 검증 시도 여부 필드 추가
data class OwnerSignUpUiState(
    val currentStep: SignUpStep = SignUpStep.STEP_1_BASIC,
    val isNextButtonEnabled: Boolean = false,

    // 이메일
    val email: String = "",
    val emailError: String? = null,
    val isEmailCodeSent: Boolean = false,
    val isEmailVerified: Boolean = false,
    // ★ 추가됨: 확인 버튼을 눌렀는지 여부 (성공/실패 무관하게 비활성화용)
    val isEmailVerificationAttempted: Boolean = false,
    val emailTimerText: String? = null,
    val isEmailTimerExpired: Boolean = false,
    val authCode: String = "",

    // 비밀번호
    val password: String = "",
    val passwordError: String? = null,
    val passwordCheck: String = "",
    val passwordCheckError: String? = null,

    // 휴대폰
    val phone: String = "",
    val isPhoneCodeSent: Boolean = false,
    val isPhoneVerified: Boolean = false,
    // ★ 추가됨: 확인 버튼을 눌렀는지 여부
    val isPhoneVerificationAttempted: Boolean = false,
    val phoneTimerText: String? = null,
    val isPhoneTimerExpired: Boolean = false,
    val phoneAuthCode: String = "",
)

sealed interface SignUpAction {
    data class UpdateEmail(val email: String) : SignUpAction
    data class UpdateAuthCode(val code: String) : SignUpAction
    data class UpdatePassword(val password: String) : SignUpAction
    data class UpdatePasswordCheck(val check: String) : SignUpAction
    data class UpdatePhone(val phone: String) : SignUpAction
    data class UpdatePhoneAuthCode(val code: String) : SignUpAction

    object RequestEmailCode : SignUpAction
    object VerifyEmailCode : SignUpAction
    object RequestPhoneCode : SignUpAction
    object VerifyPhoneCode : SignUpAction

    object OnNextClick : SignUpAction
    object OnBackClick : SignUpAction
}

sealed interface SignUpEvent {
    object NavigateBack : SignUpEvent
    object NavigateToHome : SignUpEvent
}