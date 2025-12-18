package com.gmg.seatnow.presentation.owner.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.usecase.OwnerAuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
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
    private val passwordRegex =
        "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!@#\$%^&*()_+=-]).{8,20}\$".toRegex()

    private val _storeSearchQuery = MutableSharedFlow<String>()

    init {
        // 검색어 입력 감지 및 디바운싱 처리
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            _storeSearchQuery
                .debounce(500) // 0.5초 디바운스
                .collect { query ->
                    if (query.isNotBlank()) {
                        authUseCase.searchStore(query)
                            .onSuccess { results ->
                                _uiState.update {
                                    it.copy(
                                        storeSearchResults = results,
                                        isStoreSearchDropdownExpanded = true
                                    )
                                }
                            }
                    } else {
                        _uiState.update {
                            it.copy(
                                storeSearchResults = emptyList(),
                                isStoreSearchDropdownExpanded = false
                            )
                        }
                    }
                }
        }
    }

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

            // Step 2 Implementations
            is SignUpAction.UpdateRepName -> {
                _uiState.update { it.copy(repName = action.name) }
            }

            is SignUpAction.UpdateBusinessNum -> {
                // 숫자만 입력 가능 & 최대 10자리
                if (action.num.length <= 10 && action.num.all { it.isDigit() }) {
                    _uiState.update { it.copy(businessNumber = action.num) }
                }
            }

            is SignUpAction.VerifyBusinessNum -> verifyBusinessNumber()
            is SignUpAction.UpdateStoreName -> {
                _uiState.update { it.copy(storeName = action.name) }
                viewModelScope.launch { _storeSearchQuery.emit(action.name) }
            }

            is SignUpAction.SelectStoreName -> {
                _uiState.update {
                    it.copy(
                        storeName = action.name,
                        isStoreSearchDropdownExpanded = false,
                        storeSearchResults = emptyList()
                    )
                }
            }

            is SignUpAction.OnAddressClick -> simulateAddressSelection()
            is SignUpAction.UpdateStoreContact -> {
                // 숫자만 입력, 최대 11자리
                if (action.phone.length <= 11 && action.phone.all { it.isDigit() }) {
                    _uiState.update { it.copy(storeContact = action.phone) }
                }
            }
        }
        checkNextButtonEnabled()
    }

    //Step1
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
                            isEmailVerificationAttempted = false,
                            emaiilVerifedError = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                    it.copy(
                        emailError = exception.message ?: "인증번호 전송에 실패했습니다."
                    )
                } }
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
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                          emaiilVerifedError = exception.message ?: "인증에 실패했습니다. 다시 시도해주세요."
                        )
                    } }
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
                            isPhoneVerificationAttempted = false,
                            phoneVerifedError = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            phoneError = exception.message ?: "인증번호 전송에 실패했습니다."
                        )
                    } }
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
                    _uiState.update { it.copy(isPhoneVerified = true, phoneTimerText = null, phoneVerifedError = null) }
                    checkNextButtonEnabled()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            phoneVerifedError = exception.message ?: "인증에 실패했습니다. 다시 시도해주세요."
                        )
                    } }
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
        val error =
            if (email.isNotBlank() && !email.matches(emailRegex)) "올바른 이메일 형식이 아닙니다." else null
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
        val error =
            if (password.isNotBlank() && !password.matches(passwordRegex)) "영문, 숫자, 특수문자 포함 8~20자리여야 합니다." else null
        _uiState.update { it.copy(password = password, passwordError = error) }
        validateAndUpdatePasswordCheck(_uiState.value.passwordCheck)
    }

    private fun validateAndUpdatePasswordCheck(check: String) {
        val currentPassword = _uiState.value.password
        val error = if (check.isNotBlank() && check != currentPassword) "비밀번호가 일치하지 않습니다." else null
        _uiState.update { it.copy(passwordCheck = check, passwordCheckError = error) }
    }

    private fun verifyBusinessNumber() {
        val num = _uiState.value.businessNumber
        if (num.length != 10) return

        viewModelScope.launch {
            authUseCase.verifyBusinessNumber(num)
                .onSuccess {
                    _uiState.update { it.copy(isBusinessNumVerified = true) }
                    checkNextButtonEnabled()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            businessNumberError = exception.message ?: "인증에 실패했습니다. 다시 확인해주세요."
                        )
                    } }
        }
    }

    private fun simulateAddressSelection() {
        // Mock: 주소 API 선택 시나리오
        val mockAddress = "서울특별시 서대문구 거북골로 34"
        val mockZip = "03722"

        _uiState.update {
            it.copy(
                mainAddress = mockAddress,
                zipCode = mockZip
            )
        }

        // 주소 선택 후 주변 대학 자동 조회
        viewModelScope.launch {
            authUseCase.getNearbyUniversity(mockAddress)
                .onSuccess { univ ->
                    _uiState.update { it.copy(nearbyUniv = univ) }
                    checkNextButtonEnabled()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            nearbyUnivError = exception.message ?: "근처 대학 정보 조회에 실패했습니다."
                        )
                    } }
        }
    }


    private fun checkNextButtonEnabled() {
        val state = _uiState.value

        // 1. 현재 단계(currentStep)에 따라 통과 여부(isValid)를 결정합니다.
        val isValid = when (state.currentStep) {

            // [Step 1] 기본 정보 & 약관
            SignUpStep.STEP_1_BASIC -> {
                state.isEmailVerified &&
                        state.isPhoneVerified &&
                        state.password.isNotBlank() && state.passwordError == null &&
                        state.passwordCheck.isNotBlank() && state.passwordCheckError == null &&
                        state.isAgeVerified && state.isServiceVerified &&
                        state.isPrivacyCollectVerified && state.isPrivacyProvideVerified
            }

            // [Step 2] 사업자 정보
            SignUpStep.STEP_2_BUSINESS -> {
                state.repName.isNotBlank() &&
                        state.isBusinessNumVerified &&     // 사업자 번호 인증 완료
                        state.storeName.isNotBlank() &&    // 상호명 입력 완료
                        state.mainAddress.isNotBlank() &&  // 주소 입력 완료
                        state.nearbyUniv.isNotBlank()      // 주변 대학 자동 입력 완료
                // (가게 연락처, 파일 업로드는 선택 사항이므로 조건에서 제외)
            }

            // [그 외 단계] 아직 구현 안 함 -> 일단 false
            else -> false
        }

        // 2. 결정된 값으로 UI 상태를 '한 번만' 업데이트합니다.
        _uiState.update { it.copy(isNextButtonEnabled = isValid) }
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
        val emaiilVerifedError: String? = null,
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
        val phoneError: String? = null,
        val isPhoneCodeSent: Boolean = false,
        val isPhoneVerified: Boolean = false,
        val isPhoneVerificationAttempted: Boolean = false,
        val phoneVerifedError: String? = null,
        val phoneTimerText: String? = null,
        val isPhoneTimerExpired: Boolean = false,
        val phoneAuthCode: String = "",

        // Step 2 State
        val repName: String = "",
        val businessNumber: String = "",
        val isBusinessNumVerified: Boolean = false, // 인증 완료 시 true (수정 불가)
        val businessNumberError: String? = null,

        val storeName: String = "",
        val storeSearchResults: List<String> = emptyList(), // 검색 결과
        val isStoreSearchDropdownExpanded: Boolean = false,

        val mainAddress: String = "", // 도로명 주소
        val zipCode: String = "",     // 우편 번호
        val nearbyUniv: String = "",  // 주변 대학
        val nearbyUnivError: String? = null,

        val storeContact: String = "",

        val licenseFileName: String? = null // 파일명 (선택 시 표시용)
    )

    sealed interface SignUpAction {
        // Step 1 Actions
        data class UpdateEmail(val email: String) : SignUpAction
        data class UpdateAuthCode(val code: String) : SignUpAction
        data class UpdatePassword(val password: String) : SignUpAction
        data class UpdatePasswordCheck(val check: String) : SignUpAction
        data class UpdatePhone(val phone: String) : SignUpAction
        data class UpdatePhoneAuthCode(val code: String) : SignUpAction

        data class ToggleAllTerms(val isChecked: Boolean) : SignUpAction
        data class ToggleTerm(val termType: TermType) : SignUpAction
        data class OpenTermDetail(val termType: TermType) : SignUpAction
        object CloseTermDetail : SignUpAction

        object RequestEmailCode : SignUpAction
        object VerifyEmailCode : SignUpAction
        object RequestPhoneCode : SignUpAction
        object VerifyPhoneCode : SignUpAction

        // Step 2 Actions
        data class UpdateRepName(val name: String) : SignUpAction
        data class UpdateBusinessNum(val num: String) : SignUpAction
        object VerifyBusinessNum : SignUpAction
        data class UpdateStoreName(val name: String) : SignUpAction // 검색 트리거
        data class SelectStoreName(val name: String) : SignUpAction // 검색 결과 선택
        object OnAddressClick : SignUpAction // 주소 클릭 (Mock API 호출)
        data class UpdateStoreContact(val phone: String) : SignUpAction
        // 파일 업로드 (생략 - 버튼만 활성화)

        object OnNextClick : SignUpAction
        object OnBackClick : SignUpAction
    }

    sealed interface SignUpEvent {
        object NavigateBack : SignUpEvent
        object NavigateToHome : SignUpEvent
    }
}