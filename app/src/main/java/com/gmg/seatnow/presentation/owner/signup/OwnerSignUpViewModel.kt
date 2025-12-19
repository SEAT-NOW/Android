package com.gmg.seatnow.presentation.owner.signup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.data.repository.ImageRepository
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
    private val authUseCase: OwnerAuthUseCase,
    private val imageRepository: ImageRepository
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
        // 검색어 입력 감지 및 디바운싱 처리 (기존 유지)
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            _storeSearchQuery
                .debounce(500)
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

            is SignUpAction.ToggleAllTerms -> toggleAllTerms(action.isChecked)
            is SignUpAction.ToggleTerm -> toggleSingleTerm(action.termType)
            is SignUpAction.OpenTermDetail -> _uiState.update { it.copy(openedTermType = action.termType) }
            is SignUpAction.CloseTermDetail -> _uiState.update { it.copy(openedTermType = null) }

            is SignUpAction.OnNextClick -> handleNextStep()
            is SignUpAction.OnBackClick -> handleBackStep()

            // Step 2 Implementations
            is SignUpAction.UpdateRepName -> { _uiState.update { it.copy(repName = action.name) } }
            is SignUpAction.UpdateBusinessNum -> {
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
                    )
                }
            }
            is SignUpAction.UpdateStoreContact -> {
                if (action.phone.length <= 11 && action.phone.all { it.isDigit() }) {
                    _uiState.update { it.copy(storeContact = action.phone) }
                }
            }

            // --- 주소 검색 및 파일 업로드 Action 처리 ---
            is SignUpAction.OnAddressClick -> _uiState.update { it.copy(isAddressSearchVisible = true) }
            is SignUpAction.CloseAddressSearch -> _uiState.update { it.copy(isAddressSearchVisible = false) }
            is SignUpAction.AddressSelected -> {
                handleAddressSelected(action.zoneCode, action.address)
                _uiState.update { it.copy(isAddressSearchVisible = false) }
            }
            is SignUpAction.UploadLicenseImage -> uploadLicenseImage(action.uri)
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
            val allChecked = newState.isAgeVerified && newState.isServiceVerified &&
                    newState.isPrivacyCollectVerified && newState.isPrivacyProvideVerified
            newState.copy(isAllTermsAgreed = allChecked)
        }
    }

    private fun requestEmailCode() {
        val email = _uiState.value.email
        if (email.isBlank() || _uiState.value.emailError != null) return
        viewModelScope.launch {
            authUseCase.requestAuthCode(email)
                .onSuccess {
                    startEmailTimer()
                    _uiState.update { it.copy(isEmailCodeSent = true, authCode = "", isEmailVerificationAttempted = false, emaiilVerifedError = null) }
                }
                .onFailure { exception -> _uiState.update { it.copy(emailError = exception.message ?: "인증번호 전송에 실패했습니다.") } }
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
                .onFailure { exception -> _uiState.update { it.copy(emaiilVerifedError = exception.message ?: "인증에 실패했습니다. 다시 시도해주세요.") } }
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

    private fun requestPhoneCode() {
        val phone = _uiState.value.phone
        if (phone.length < 10) return
        viewModelScope.launch {
            authUseCase.requestAuthCode(phone)
                .onSuccess {
                    startPhoneTimer()
                    _uiState.update { it.copy(isPhoneCodeSent = true, phoneAuthCode = "", isPhoneVerificationAttempted = false, phoneVerifedError = null) }
                }
                .onFailure { exception -> _uiState.update { it.copy(phoneError = exception.message ?: "인증번호 전송에 실패했습니다.") } }
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
                .onFailure { exception -> _uiState.update { it.copy(phoneVerifedError = exception.message ?: "인증에 실패했습니다. 다시 시도해주세요.") } }
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
        _uiState.update { it.copy(email = email, emailError = error, isEmailVerified = false, isEmailCodeSent = false, isEmailVerificationAttempted = false) }
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

    private fun verifyBusinessNumber() {
        val num = _uiState.value.businessNumber
        if (num.length != 10) return
        viewModelScope.launch {
            authUseCase.verifyBusinessNumber(num)
                .onSuccess {
                    _uiState.update { it.copy(isBusinessNumVerified = true, businessNumberError = null) }
                    checkNextButtonEnabled()
                }
                .onFailure { exception -> _uiState.update { it.copy(businessNumberError = exception.message ?: "인증에 실패했습니다. 다시 확인해주세요.") } }
        }
    }

    private fun uploadLicenseImage(uri: Uri) {
        viewModelScope.launch {
            imageRepository.uploadImage(uri)
                .onSuccess { imageUrl ->
                    _uiState.update {
                        it.copy(
                            licenseFileName = "등록 완료",
                            licenseImageUrl = imageUrl // ★ 중요: State에 이 필드가 있어야 오류가 안 납니다.
                        )
                    }
                    checkNextButtonEnabled()
                }
                .onFailure {
                    _uiState.update { it.copy(licenseFileName = "업로드 실패: 다시 시도해주세요") }
                }
        }
    }

    private fun handleAddressSelected(zoneCode: String, address: String) {
        _uiState.update { it.copy(mainAddress = address, zipCode = zoneCode) }
        viewModelScope.launch {
            authUseCase.getNearbyUniversity(address)
                .onSuccess { univ ->
                    _uiState.update { it.copy(nearbyUniv = univ) }
                    checkNextButtonEnabled()
                }
        }
    }

    private fun checkNextButtonEnabled() {
        val state = _uiState.value
        val isValid = when (state.currentStep) {
            SignUpStep.STEP_1_BASIC -> {
                state.isEmailVerified && state.isPhoneVerified &&
                        state.password.isNotBlank() && state.passwordError == null &&
                        state.passwordCheck.isNotBlank() && state.passwordCheckError == null &&
                        state.isAgeVerified && state.isServiceVerified &&
                        state.isPrivacyCollectVerified && state.isPrivacyProvideVerified
            }
            SignUpStep.STEP_2_BUSINESS -> {
                state.repName.isNotBlank() &&
                        state.isBusinessNumVerified &&
                        state.storeName.isNotBlank() &&
                        state.mainAddress.isNotBlank() &&
                        state.nearbyUniv.isNotBlank()
            }
            else -> false
        }
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

    // ★ [수정됨] 뒤로가기 처리: 주소 검색창이 열려있으면 그것만 닫습니다.
    private fun handleBackStep() {
        // 1. 주소 검색창이 열려있으면 닫기
        if (_uiState.value.isAddressSearchVisible) {
            _uiState.update { it.copy(isAddressSearchVisible = false) }
            return
        }

        // 2. 약관 상세 화면이 열려있으면 닫기
        if (_uiState.value.openedTermType != null) {
            _uiState.update { it.copy(openedTermType = null) }
            return
        }

        // 3. 이전 단계로 이동 or 화면 종료
        val currentStep = _uiState.value.currentStep
        val prevOrdinal = currentStep.ordinal - 1
        if (prevOrdinal >= 0) {
            _uiState.update { it.copy(currentStep = SignUpStep.entries[prevOrdinal]) }
        } else {
            viewModelScope.launch { _event.emit(SignUpEvent.NavigateBack) }
        }
    }

    enum class TermType(val title: String) {
        AGE("[필수] 만 14세 이상"),
        SERVICE("[필수] 이용약관 동의"),
        PRIVACY_COLLECT("[필수] 개인정보 수집이용 동의"),
        PRIVACY_PROVIDE("[필수] 개인정보 처리방침 동의")
    }

    data class OwnerSignUpUiState(
        val currentStep: SignUpStep = SignUpStep.STEP_2_BUSINESS,
        val isNextButtonEnabled: Boolean = false,

        // 약관 관련
        val isAllTermsAgreed: Boolean = false,
        val isAgeVerified: Boolean = false,
        val isServiceVerified: Boolean = false,
        val isPrivacyCollectVerified: Boolean = false,
        val isPrivacyProvideVerified: Boolean = false,
        val openedTermType: TermType? = null,

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

        // 비밀번호, 휴대폰
        val password: String = "",
        val passwordError: String? = null,
        val passwordCheck: String = "",
        val passwordCheckError: String? = null,
        val phone: String = "",
        val phoneError: String? = null,
        val isPhoneCodeSent: Boolean = false,
        val isPhoneVerified: Boolean = false,
        val isPhoneVerificationAttempted: Boolean = false,
        val phoneVerifedError: String? = null,
        val phoneTimerText: String? = null,
        val isPhoneTimerExpired: Boolean = false,
        val phoneAuthCode: String = "",

        // Step 2
        val repName: String = "",
        val businessNumber: String = "",
        val isBusinessNumVerified: Boolean = false,
        val businessNumberError: String? = null,
        val storeName: String = "",
        val storeSearchResults: List<String> = emptyList(),
        val isStoreSearchDropdownExpanded: Boolean = false,
        val mainAddress: String = "",
        val zipCode: String = "",
        val nearbyUniv: String = "",
        val nearbyUnivError: String? = null,
        val storeContact: String = "",

        val isAddressSearchVisible: Boolean = false,
        val licenseFileName: String? = null,

        // ★ [추가됨] 서버에서 받은 이미지 URL을 저장할 변수
        val licenseImageUrl: String? = null
    )

    sealed interface SignUpAction {
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

        data class UpdateRepName(val name: String) : SignUpAction
        data class UpdateBusinessNum(val num: String) : SignUpAction
        object VerifyBusinessNum : SignUpAction
        data class UpdateStoreName(val name: String) : SignUpAction
        data class SelectStoreName(val name: String) : SignUpAction
        object OnAddressClick : SignUpAction
        object CloseAddressSearch : SignUpAction // ★
        data class AddressSelected(val zoneCode: String, val address: String) : SignUpAction
        data class UpdateStoreContact(val phone: String) : SignUpAction
        data class UploadLicenseImage(val uri: Uri) : SignUpAction
        object OnNextClick : SignUpAction
        object OnBackClick : SignUpAction
    }

    sealed interface SignUpEvent {
        object NavigateBack : SignUpEvent
        object NavigateToHome : SignUpEvent
    }
}