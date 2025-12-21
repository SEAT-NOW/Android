package com.gmg.seatnow.presentation.owner.signup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.data.repository.ImageRepository
import com.gmg.seatnow.domain.model.StoreSearchResult
import com.gmg.seatnow.domain.usecase.OwnerAuthUseCase
import com.gmg.seatnow.presentation.owner.dataClass.SpaceItem
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
        // ★ [Step 2] 상호명 검색 디바운싱 로직
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            _storeSearchQuery
                .debounce(500)
                .collect { query ->
                    if (query.isNotBlank()) {
                        authUseCase.searchStore(query)
                            .onSuccess { results ->
                                _uiState.update { it.copy(storeSearchResults = results) }
                            }
                    } else {
                        _uiState.update { it.copy(storeSearchResults = emptyList()) }
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

            // Step 2 Implementations
            is SignUpAction.UpdateRepName -> { _uiState.update { it.copy(repName = action.name) } }
            is SignUpAction.UpdateBusinessNum -> {
                if (action.num.length <= 10 && action.num.all { it.isDigit() }) {
                    _uiState.update { it.copy(businessNumber = action.num) }
                }
            }
            is SignUpAction.VerifyBusinessNum -> verifyBusinessNumber()
            // ★ [변경] 상호명 검색 관련 Action 처리
            is SignUpAction.OpenStoreSearch -> _uiState.update { it.copy(isStoreSearchVisible = true) }
            is SignUpAction.CloseStoreSearch -> _uiState.update { it.copy(isStoreSearchVisible = false) }
            is SignUpAction.SearchStoreQuery -> {
                viewModelScope.launch { _storeSearchQuery.emit(action.query) }
            }
            is SignUpAction.SelectStore -> selectStore(action.store)

            // ★ [변경] 주소 직접 수정 Action (우편번호 삭제됨)
            is SignUpAction.UpdateMainAddress -> _uiState.update { it.copy(mainAddress = action.address) }

            is SignUpAction.UpdateStoreContact -> {
                if (action.phone.length <= 11 && action.phone.all { it.isDigit() }) {
                    _uiState.update { it.copy(storeContact = action.phone) }
                }
            }
            is SignUpAction.UploadLicenseImage -> uploadLicenseImage(action.uri, action.fileName)

            is SignUpAction.UpdateSpaceInput -> _uiState.update { it.copy(spaceInput = action.input) }
            is SignUpAction.UpdateTablePersonCount -> _uiState.update { it.copy(tablePersonCount = action.count) }
            is SignUpAction.UpdateTableCount -> _uiState.update { it.copy(tableCount = action.count) }

            is SignUpAction.UpdateSpaceInput -> {
                _uiState.update { it.copy(spaceInput = action.input, spaceInputError = null) }
            }
            is SignUpAction.AddSpace -> addSpaceItem()
            is SignUpAction.RemoveSpace -> removeSpaceItem(action.id)
            is SignUpAction.EditSpace -> toggleEditMode(action.id, true)
            is SignUpAction.UpdateEditInput -> updateEditInput(action.id, action.input)
            is SignUpAction.SaveSpace -> saveSpaceItem(action.id)

            is SignUpAction.OnNextClick -> handleNextStep()
            is SignUpAction.OnBackClick -> handleBackStep()
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

    //Step2
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

    private fun selectStore(store: StoreSearchResult) {
        _uiState.update {
            it.copy(
                storeName = store.placeName,     // 상호명 자동 입력
                mainAddress = store.addressName, // 주소 자동 입력
                isStoreSearchVisible = false,    // 검색창 닫기
                nearbyUniv = "대학 검색 중...",   // 로딩 표시
                isNearbyUnivEnabled = true      // 아직 입력 불가
            )
        }

        // 위도/경도로 대학 찾기 API 호출
        viewModelScope.launch {
            authUseCase.getNearbyUniversity(store.latitude, store.longitude)
                .onSuccess { univ ->
                    _uiState.update {
                        it.copy(
                            nearbyUniv = univ,
                            isNearbyUnivEnabled = false // ★ 성공 시 활성화
                        )
                    }
                    checkNextButtonEnabled()
                }
                .onFailure {
                    _uiState.update {
                        it.copy(nearbyUniv = "대학을 찾을 수 없습니다.", isNearbyUnivEnabled = false)
                    }
                }
        }
    }

    private fun uploadLicenseImage(uri: Uri, fileName: String) {
        // 1. 먼저 UI에 파일명 표시 (사용자에게 즉각 반응)
        _uiState.update {
            it.copy(licenseFileName = fileName)
        }

        viewModelScope.launch {
            // 2. 실제 업로드 로직 수행
            imageRepository.uploadImage(uri)
                .onSuccess { imageUrl ->
                    _uiState.update {
                        it.copy(
                            licenseImageUrl = imageUrl,
                            // 업로드 성공 시 파일명을 그대로 유지하거나 "업로드 완료" 등으로 변경 가능
                            licenseFileName = fileName
                        )
                    }
                    checkNextButtonEnabled()
                }
                .onFailure {
                    _uiState.update {
                        it.copy(licenseFileName = "업로드 실패: 다시 선택해주세요")
                    }
                }
        }
    }

    private fun addSpaceItem() {
        val input = _uiState.value.spaceInput.trim()
        if (input.isBlank()) {
            _uiState.update { it.copy(spaceInputError = "텍스트를 입력해주세요.") }
            return
        }

        val newItem = SpaceItem(name = input)
        _uiState.update {
            it.copy(
                spaceList = it.spaceList + newItem,
                spaceInput = "", // 입력창 초기화
                spaceInputError = null
            )
        }
    }

    private fun removeSpaceItem(id: Long) {
        _uiState.update { state ->
            state.copy(spaceList = state.spaceList.filter { it.id != id })
        }
    }

    private fun toggleEditMode(id: Long, isEditing: Boolean) {
        _uiState.update { state ->
            state.copy(spaceList = state.spaceList.map { item ->
                if (item.id == id) {
                    // 수정 시작할 때 현재 이름을 editInput에 복사
                    item.copy(isEditing = isEditing, editInput = if(isEditing) item.name else "")
                } else item
            })
        }
    }

    private fun updateEditInput(id: Long, input: String) {
        _uiState.update { state ->
            state.copy(spaceList = state.spaceList.map {
                if (it.id == id) it.copy(editInput = input) else it
            })
        }
    }

    private fun saveSpaceItem(id: Long) {
        _uiState.update { state ->
            state.copy(spaceList = state.spaceList.map { item ->
                if (item.id == id) {
                    // 입력값이 비어있으면 저장 안 함 (혹은 에러 처리)
                    if (item.editInput.isBlank()) item
                    else item.copy(name = item.editInput, isEditing = false)
                } else item
            })
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
            SignUpStep.STEP_3_STORE -> state.spaceList.isNotEmpty()
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
        if (_uiState.value.isStoreSearchVisible) {
            _uiState.update { it.copy(isStoreSearchVisible = false) }
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
        val currentStep: SignUpStep = SignUpStep.STEP_3_STORE,
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
        val mainAddress: String = "",
        val nearbyUniv: String = "",
        val isNearbyUnivEnabled: Boolean = true,
        val nearbyUnivError: String? = null,
        val storeContact: String = "",
        val licenseFileName: String? = null,
        val licenseImageUrl: String? = null,
        val isStoreSearchVisible: Boolean = false,
        val storeSearchResults: List<StoreSearchResult> = emptyList(),

        // ★ [Step 3 추가] 공간/테이블 구성 관련 State
        val spaceInput: String = "",
        val tablePersonCount: String = "", // N (인원)
        val tableCount: String = "",       // M (개수)
        val spaceInputError: String? = null, // 에러 메시지용
        val spaceList: List<SpaceItem> = emptyList(), // 추가된 공간 리스트
    )

    sealed interface SignUpAction {
        //step1
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

        //step2
        data class UpdateRepName(val name: String) : SignUpAction
        data class UpdateBusinessNum(val num: String) : SignUpAction
        object VerifyBusinessNum : SignUpAction
        object OpenStoreSearch : SignUpAction
        object CloseStoreSearch : SignUpAction
        data class SearchStoreQuery(val query: String) : SignUpAction
        data class SelectStore(val store: StoreSearchResult) : SignUpAction
        data class UpdateMainAddress(val address: String) : SignUpAction
        data class UpdateStoreContact(val phone: String) : SignUpAction
        data class UploadLicenseImage(val uri: Uri, val fileName: String) : SignUpAction
        //step3
        data class UpdateSpaceInput(val input: String) : SignUpAction
        data class UpdateTablePersonCount(val count: String) : SignUpAction
        data class UpdateTableCount(val count: String) : SignUpAction

        object AddSpace : SignUpAction // 플러스 버튼 클릭 시
        data class RemoveSpace(val id: Long) : SignUpAction
        data class EditSpace(val id: Long) : SignUpAction // 수정 버튼 클릭 (Read -> Edit)
        data class UpdateEditInput(val id: Long, val input: String) : SignUpAction // 수정 중 텍스트 변경
        data class SaveSpace(val id: Long) : SignUpAction // 완료 버튼 클릭 (Edit -> Read)


        object OnNextClick : SignUpAction
        object OnBackClick : SignUpAction
    }

    sealed interface SignUpEvent {
        object NavigateBack : SignUpEvent
        object NavigateToHome : SignUpEvent
    }
}