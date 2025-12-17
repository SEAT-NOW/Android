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

            // ★ 약관 관련 액션 추가
            is SignUpAction.ToggleAllTerms -> toggleAllTerms(action.isChecked)
            is SignUpAction.ToggleTerm -> toggleSingleTerm(action.termType)
            is SignUpAction.OpenTermDetail -> _uiState.update { it.copy(openedTermType = action.termType) }
            is SignUpAction.CloseTermDetail -> _uiState.update { it.copy(openedTermType = null) }

            is SignUpAction.OnNextClick -> handleNextStep()
            is SignUpAction.OnBackClick -> handleBackStep()
        }
        checkNextButtonEnabled()
    }

    // --- 약관 동의 로직 ---
    private fun toggleAllTerms(isChecked: Boolean) {
        _uiState.update {
            it.copy(
                isAllTermsAgreed = isChecked,
                isAgeVerified = isChecked,
                isServiceVerified = isChecked,
                isPrivacyCollectVerified = isChecked,
                isPrivacyProvideVerified = isChecked
            )
        }
    }

    private fun toggleSingleTerm(termType: TermType) {
        _uiState.update { state ->
            val newState = when (termType) {
                TermType.AGE -> state.copy(isAgeVerified = !state.isAgeVerified)
                TermType.SERVICE -> state.copy(isServiceVerified = !state.isServiceVerified)
                TermType.PRIVACY_COLLECT -> state.copy(isPrivacyCollectVerified = !state.isPrivacyCollectVerified)
                TermType.PRIVACY_PROVIDE -> state.copy(isPrivacyProvideVerified = !state.isPrivacyProvideVerified)
            }
            // 개별 선택 후 "모두 선택" 상태 동기화
            val allChecked = newState.isAgeVerified && newState.isServiceVerified &&
                    newState.isPrivacyCollectVerified && newState.isPrivacyProvideVerified
            newState.copy(isAllTermsAgreed = allChecked)
        }
    }

    // --- 이메일 인증 로직 ---
    private fun requestEmailCode() {
        val email = _uiState.value.email
        if (email.isBlank() || _uiState.value.emailError != null) return

        viewModelScope.launch {
            authUseCase.requestAuthCode(email)
                .onSuccess {
                    startEmailTimer()
                    _uiState.update {
                        it.copy(
                            isEmailCodeSent = true,
                            authCode = "",
                            isEmailVerificationAttempted = false
                        )
                    }
                }
                .onFailure { /* 에러 처리 */ }
        }
    }

    private fun verifyEmailCode() {
        val email = _uiState.value.email
        val code = _uiState.value.authCode
        _uiState.update { it.copy(isEmailVerificationAttempted = true) }
        stopEmailTimer()

        viewModelScope.launch {
            authUseCase.verifyAuthCode(email, code)
                .onSuccess {
                    _uiState.update { it.copy(isEmailVerified = true, emailTimerText = null) }
                    checkNextButtonEnabled()
                }
        }
    }

    private fun startEmailTimer() {
        emailTimerJob?.cancel()
        emailTimerJob = viewModelScope.launch {
            var time = 180
            _uiState.update { it.copy(isEmailTimerExpired = false) }
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

    private fun validateAndUpdateEmail(email: String) {
        val error = if (email.isNotBlank() && !email.matches(emailRegex)) "올바른 이메일 형식이 아닙니다." else null
        _uiState.update {
            it.copy(
                email = email,
                emailError = error,
                isEmailVerified = false,
                isEmailCodeSent = false,
                isEmailVerificationAttempted = false
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
        // ★ [핵심] 다음 버튼 활성화 조건에 "약관 동의" 추가
        val isStep1Valid = s.isEmailVerified &&
                s.isPhoneVerified &&
                s.password.isNotBlank() && s.passwordError == null &&
                s.passwordCheck.isNotBlank() && s.passwordCheckError == null &&
                // 모든 필수 약관이 동의되어야 함
                s.isAgeVerified && s.isServiceVerified && s.isPrivacyCollectVerified && s.isPrivacyProvideVerified

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
        // ★ 약관 상세 화면이 열려있다면 닫기
        if (_uiState.value.openedTermType != null) {
            _uiState.update { it.copy(openedTermType = null) }
            return
        }

        val currentStep = _uiState.value.currentStep
        val prevOrdinal = currentStep.ordinal - 1
        if (prevOrdinal >= 0) {
            _uiState.update { it.copy(currentStep = SignUpStep.entries[prevOrdinal]) }
        } else {
            viewModelScope.launch { _event.emit(SignUpEvent.NavigateBack) }
        }
    }
}

// 약관 종류 구분용 Enum
enum class TermType(val title: String) {
    AGE("[필수] 만 14세 이상"),
    SERVICE("[필수] 이용약관 동의"),
    PRIVACY_COLLECT("[필수] 개인정보 수집이용 동의"),
    PRIVACY_PROVIDE("[필수] 개인정보 처리방침 동의")
}

data class OwnerSignUpUiState(
    val currentStep: SignUpStep = SignUpStep.STEP_1_BASIC,
    val isNextButtonEnabled: Boolean = false,

    // 약관 관련 상태
    val isAllTermsAgreed: Boolean = false,
    val isAgeVerified: Boolean = false,
    val isServiceVerified: Boolean = false,
    val isPrivacyCollectVerified: Boolean = false,
    val isPrivacyProvideVerified: Boolean = false,
    val openedTermType: TermType? = null, // 현재 열린 약관 상세 (null이면 안 열림)

    // 이메일
    val email: String = "",
    val emailError: String? = null,
    val isEmailCodeSent: Boolean = false,
    val isEmailVerified: Boolean = false,
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

    // 약관 관련 액션
    data class ToggleAllTerms(val isChecked: Boolean) : SignUpAction
    data class ToggleTerm(val termType: TermType) : SignUpAction
    data class OpenTermDetail(val termType: TermType) : SignUpAction
    object CloseTermDetail : SignUpAction

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