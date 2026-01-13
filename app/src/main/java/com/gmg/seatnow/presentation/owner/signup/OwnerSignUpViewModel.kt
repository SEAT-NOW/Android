package com.gmg.seatnow.presentation.owner.signup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.data.repository.ImageRepository
import com.gmg.seatnow.domain.model.StoreSearchResult
import com.gmg.seatnow.domain.model.OperatingScheduleItem
import com.gmg.seatnow.domain.model.SpaceItem
import com.gmg.seatnow.domain.model.SignUpTableItem
import dagger.hilt.android.lifecycle.HiltViewModel
import com.gmg.seatnow.domain.usecase.auth.*
import com.gmg.seatnow.domain.usecase.logic.*
import com.gmg.seatnow.domain.usecase.store.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerSignUpViewModel @Inject constructor(
    // [Auth UseCases]
    private val requestPhoneAuthCodeUseCase: RequestPhoneAuthCodeUseCase, // 이메일/폰 공용
    private val requestEmailAuthCodeUseCase: RequestEmailAuthCodeUseCase,
    private val verifyPhoneAuthCodeUseCase: VerifyPhoneAuthCodeUseCase,
    private val verifyEmailAuthCodeUseCase: VerifyEmailAuthCodeUseCase,   // 이메일/폰 공용
    private val verifyBusinessNumberUseCase: VerifyBusinessNumberUseCase,

    // [Store UseCases]
    private val searchStoreUseCase: SearchStoreUseCase,
    private val getNearbyUniversityUseCase: GetNearbyUniversityUseCase,

    // [Logic UseCases]
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val formatTimerUseCase: FormatTimerUseCase,
    private val calculateSpaceInfoUseCase: CalculateSpaceInfoUseCase,

    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerSignUpUiState(
        weeklyHolidayDays = setOf(0),
        monthlyHolidayDays = setOf(0),
        monthlyHolidayWeeks = setOf(2,4)
    ))
    val uiState: StateFlow<OwnerSignUpUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<SignUpEvent>()
    val event: SharedFlow<SignUpEvent> = _event.asSharedFlow()

    private var emailTimerJob: Job? = null
    private var phoneTimerJob: Job? = null

    private val _storeSearchQuery = MutableSharedFlow<String>()

    init {
        // [UseCase 적용] 상호명 검색
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            _storeSearchQuery
                .debounce(500)
                .collect { query ->
                    if (query.isNotBlank()) {
                        searchStoreUseCase(query) // UseCase 호출
                            .onSuccess { results ->
                                _uiState.update { it.copy(storeSearchResults = results) }
                            }
                    } else {
                        _uiState.update { it.copy(storeSearchResults = emptyList()) }
                    }
                }
        }
        initializeDefaultSpace()
    }

    private fun initializeDefaultSpace() {
        // [Step 3 요구사항] 기본값 "전체", 수정 모드 True로 시작
        val defaultSpace = SpaceItem(
            id = System.currentTimeMillis(),
            name = "",
            seatCount = 0,
            isEditing = true, // 수정 중 상태
            editInput = "전체", // 기본 키워드
            tableList = listOf(SignUpTableItem(personCount = "", tableCount = ""))
        )
        _uiState.update {
            it.copy(
                spaceList = listOf(defaultSpace),
                selectedSpaceId = defaultSpace.id,
                isNextButtonEnabled = false // ★ 수정 중이므로 버튼 비활성화
            )
        }
    }

    fun onAction(action: SignUpAction) {
        when (action) {
            // Step 1 Action
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
            is SignUpAction.OpenStoreSearch -> _uiState.update { it.copy(isStoreSearchVisible = true) }
            is SignUpAction.CloseStoreSearch -> _uiState.update { it.copy(isStoreSearchVisible = false) }
            is SignUpAction.SearchStoreQuery -> {
                viewModelScope.launch { _storeSearchQuery.emit(action.query) }
            }
            is SignUpAction.SelectStore -> selectStore(action.store)
            is SignUpAction.UpdateMainAddress -> _uiState.update { it.copy(mainAddress = action.address) }
            is SignUpAction.UpdateStoreContact -> {
                if (action.phone.length <= 11 && action.phone.all { it.isDigit() }) {
                    _uiState.update { it.copy(storeContact = action.phone) }
                }
            }
            is SignUpAction.UploadLicenseImage -> uploadLicenseImage(action.uri, action.fileName)

            // Step 3
            is SignUpAction.AddSpaceItemRow -> addSpaceItemRow()
            is SignUpAction.UpdateSpaceItemInput -> updateSpaceItemInput(action.id, action.input)
            is SignUpAction.SaveSpaceItem -> saveSpaceItem(action.id)
            is SignUpAction.SelectSpace -> selectSpace(action.id)
            is SignUpAction.RemoveSpace -> removeSpaceItem(action.id)
            is SignUpAction.EditSpace -> toggleEditMode(action.id)

            is SignUpAction.AddTableItemRow -> addTableItemRow()
            is SignUpAction.UpdateTableItemN -> updateTableItemValue(action.tableId, nValue = action.value, mValue = null)
            is SignUpAction.UpdateTableItemM -> updateTableItemValue(action.tableId, nValue = null, mValue = action.value)
            is SignUpAction.RemoveTableItemRow -> removeTableItemRow(action.tableId)

            // Step 4
            is SignUpAction.ToggleRegularHolidayType -> {
                _uiState.update {
                    val newType = if (it.regularHolidayType == action.type) 0 else action.type
                    it.copy(regularHolidayType = newType)
                }
            }
            is SignUpAction.UpdateWeeklyHolidays -> _uiState.update { it.copy(weeklyHolidayDays = action.days, showWeeklyDayDialog = false) }
            is SignUpAction.UpdateMonthlyWeeks -> _uiState.update { it.copy(monthlyHolidayWeeks = action.weeks, showMonthlyWeekDialog = false) }
            is SignUpAction.UpdateMonthlyDays -> _uiState.update { it.copy(monthlyHolidayDays = action.days, showMonthlyDayDialog = false) }
            is SignUpAction.ToggleTempHoliday -> _uiState.update { it.copy(isTempHolidayEnabled = !it.isTempHolidayEnabled) }
            is SignUpAction.UpdateTempHolidayRange -> {
                _uiState.update {
                    it.copy(tempHolidayStart = action.start, tempHolidayEnd = action.end, showTempHolidayDatePicker = false)
                }
            }
            is SignUpAction.AddOperatingSchedule -> {
                val newId = (_uiState.value.operatingSchedules.maxOfOrNull { it.id } ?: 0) + 1
                val newItem = OperatingScheduleItem(newId, startHour = 18, startMin = 0, endHour = 0, endMin = 0)
                _uiState.update { it.copy(operatingSchedules = it.operatingSchedules + newItem) }
            }
            is SignUpAction.UpdateOperatingDays -> updateOperatingScheduleDays(action.id, action.dayIdx)
            is SignUpAction.UpdateOperatingTime -> updateOperatingScheduleTime(action.id, action.startHour, action.startMin, action.endHour, action.endMin)
            is SignUpAction.RemoveOperatingSchedule -> {
                _uiState.update { it.copy(operatingSchedules = it.operatingSchedules.filter { item -> item.id != action.id }) }
            }

            is SignUpAction.SetWeeklyDialogVisible -> _uiState.update { it.copy(showWeeklyDayDialog = action.visible) }
            is SignUpAction.SetMonthlyWeekDialogVisible -> _uiState.update { it.copy(showMonthlyWeekDialog = action.visible) }
            is SignUpAction.SetMonthlyDayDialogVisible -> _uiState.update { it.copy(showMonthlyDayDialog = action.visible) }
            is SignUpAction.SetTempHolidayDatePickerVisible -> _uiState.update { it.copy(showTempHolidayDatePicker = action.visible) }

            //step 5
            is SignUpAction.AddStorePhotos -> addStorePhotos(action.uris)
            is SignUpAction.RemoveStorePhoto -> removeStorePhoto(action.uri)
            is SignUpAction.SetRepresentativePhoto -> setRepresentativePhoto(action.uri)

            // Navigation
            is SignUpAction.OnNextClick -> handleNextStep()
            is SignUpAction.OnBackClick -> handleBackStep()
        }

        // ★ 어떤 액션이든 끝나면 버튼 상태 체크 (특히 SaveSpaceItem 이후 중요)
        checkNextButtonEnabled()
    }

    // --- Step 1 Implementation ---

    private fun requestEmailCode() {
        val email = _uiState.value.email
        if (email.isBlank() || _uiState.value.emailError != null) return
        viewModelScope.launch {
            // [UseCase 적용] 이메일 인증 요청
            requestEmailAuthCodeUseCase(email)
                .onSuccess {
                    startEmailTimer()
                    _uiState.update {
                        it.copy(
                            isEmailCodeSent = true,
                            authCode = "",
                            isEmailVerificationAttempted = false,
                            emailVerifiedError = null) }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(emailError = exception.message ?: "인증번호 전송에 실패했습니다.") } }
        }
    }

    private fun verifyEmailCode() {
        val email = _uiState.value.email
        val code = _uiState.value.authCode
        _uiState.update { it.copy(isEmailVerificationAttempted = true) }
        stopEmailTimer()
        viewModelScope.launch {
            // [UseCase 적용] 인증번호 검증
            verifyEmailAuthCodeUseCase(email, code)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isEmailVerified = true,
                            emailTimerText = null,
                            emailVerifiedError = null) }
                    checkNextButtonEnabled()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(emailVerifiedError = exception.message ?: "인증에 실패했습니다. 다시 시도해주세요.") } }
        }
    }

    private fun requestPhoneCode() {
        val phone = _uiState.value.phone
        // 하이픈이 섞여있어도 UseCase 혹은 로직에서 제거한다고 가정 (여기선 길이만 체크)
        if (phone.length < 10) return

        viewModelScope.launch {
            // [변경] 분리된 UseCase 호출
            requestPhoneAuthCodeUseCase(phone)
                .onSuccess {
                    startPhoneTimer()
                    _uiState.update {
                        it.copy(
                            isPhoneCodeSent = true,
                            phoneAuthCode = "",
                            isPhoneVerificationAttempted = false,
                            phoneVerifiedError = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(phoneError = exception.message ?: "인증번호 전송에 실패했습니다.")
                    }
                }
        }
    }

    // [핸드폰 검증 로직]
    private fun verifyPhoneCode() {
        val phone = _uiState.value.phone
        val code = _uiState.value.phoneAuthCode
        _uiState.update { it.copy(isPhoneVerificationAttempted = true) }
        stopPhoneTimer() // 타이머 멈춤
        viewModelScope.launch {
            // [변경] 핸드폰 전용 UseCase 호출 (실제 API)
            verifyPhoneAuthCodeUseCase(phone, code)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isPhoneVerified = true,
                            phoneTimerText = null,
                            phoneVerifiedError = null
                        )
                    }
                    checkNextButtonEnabled()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(phoneVerifiedError = exception.message ?: "인증 번호가 일치하지 않습니다.")
                    }
                }
        }
    }

    private fun validateAndUpdateEmail(email: String) {
        // [UseCase 적용] 정규식 로직 제거 -> UseCase 사용
        val isValid = validateEmailUseCase(email)
        val error = if (email.isNotBlank() && !isValid) "올바른 이메일 형식이 아닙니다." else null

        _uiState.update { it.copy(email = email, emailError = error, isEmailVerified = false, isEmailCodeSent = false, isEmailVerificationAttempted = false) }
        stopEmailTimer()
    }

    private fun validateAndUpdatePassword(password: String) {
        // [UseCase 적용]
        val isValid = validatePasswordUseCase(password)
        val error = if (password.isNotBlank() && !isValid) "영문, 숫자, 특수문자 포함 8~20자리여야 합니다." else null

        _uiState.update { it.copy(password = password, passwordError = error) }
        validateAndUpdatePasswordCheck(_uiState.value.passwordCheck)
    }

    // (참고) 비밀번호 확인 일치 로직은 간단한 비교라 ViewModel에 남겨둠 (필요시 UseCase화 가능)
    private fun validateAndUpdatePasswordCheck(check: String) {
        val currentPassword = _uiState.value.password
        val error = if (check.isNotBlank() && check != currentPassword) "비밀번호가 일치하지 않습니다." else null
        _uiState.update { it.copy(passwordCheck = check, passwordCheckError = error) }
    }

    private fun startEmailTimer() {
        emailTimerJob?.cancel()
        emailTimerJob = viewModelScope.launch {
            var time = 180
            _uiState.update { it.copy(isEmailTimerExpired = false) }
            while (time > 0) {
                // [UseCase 적용] 시간 포맷팅
                val timeString = formatTimerUseCase(time)
                _uiState.update { it.copy(emailTimerText = timeString) }
                delay(1000)
                time--
            }
            _uiState.update { it.copy(emailTimerText = "0:00", isEmailTimerExpired = true) }
        }
    }

    private fun startPhoneTimer() {
        phoneTimerJob?.cancel()
        phoneTimerJob = viewModelScope.launch {
            var time = 180
            _uiState.update { it.copy(isPhoneTimerExpired = false) }
            while (time > 0) {
                // [UseCase 적용] 시간 포맷팅
                val timeString = formatTimerUseCase(time)
                _uiState.update { it.copy(phoneTimerText = timeString) }
                delay(1000)
                time--
            }
            _uiState.update { it.copy(phoneTimerText = "0:00", isPhoneTimerExpired = true) }
        }
    }

    private fun stopEmailTimer() {
        emailTimerJob?.cancel()
        _uiState.update { it.copy(emailTimerText = null) }
    }

    private fun stopPhoneTimer() {
        phoneTimerJob?.cancel()
        _uiState.update { it.copy(phoneTimerText = null) }
    }

    // 약관 관련 로직 (단순 토글이라 VM 유지)
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

    // --- Step 2 Implementation ---

    private fun verifyBusinessNumber() {
        val num = _uiState.value.businessNumber
        if (num.length != 10) return
        viewModelScope.launch {
            // [UseCase 적용]
            verifyBusinessNumberUseCase(num)
                .onSuccess {
                    _uiState.update { it.copy(isBusinessNumVerified = true, businessNumberError = null) }
                    checkNextButtonEnabled()
                }
                .onFailure { exception -> _uiState.update { it.copy(businessNumberError = exception.message ?: "인증 실패") } }
        }
    }



    private fun selectStore(store: StoreSearchResult) {
        _uiState.update {
            it.copy(
                storeName = store.placeName,
                mainAddress = store.addressName,
                isStoreSearchVisible = false,
                nearbyUniv = "대학 검색 중...",
                isNearbyUnivEnabled = true
            )
        }

        viewModelScope.launch {
            // [UseCase 적용]
            getNearbyUniversityUseCase(store.latitude, store.longitude)
                .onSuccess { univList ->
                    val resultText = if (univList.isEmpty()) "근처 대학 없음" else univList.joinToString(" / ")
                    _uiState.update { it.copy(nearbyUniv = resultText, isNearbyUnivEnabled = false) }
                    checkNextButtonEnabled()
                }
                .onFailure {
                    _uiState.update { it.copy(nearbyUniv = "대학을 찾을 수 없습니다.", isNearbyUnivEnabled = false) }
                }
        }
    }

    private fun uploadLicenseImage(uri: Uri, fileName: String) {
        _uiState.update { it.copy(licenseFileName = fileName) }
        viewModelScope.launch {
            imageRepository.uploadImage(uri)
                .onSuccess { imageUrl ->
                    _uiState.update { it.copy(licenseImageUrl = imageUrl, licenseFileName = fileName) }
                    checkNextButtonEnabled()
                }
                .onFailure {
                    _uiState.update { it.copy(licenseFileName = "업로드 실패: 다시 선택해주세요") }
                }
        }
    }

    // --- Step 3 Implementation ---

    private fun addSpaceItemRow() {
        val newItem = SpaceItem(
            id = System.currentTimeMillis(),
            name = "",
            isEditing = true, // 생성 시 수정 모드
            editInput = "",
            tableList = listOf(SignUpTableItem(personCount = "", tableCount = ""))
        )
        _uiState.update {
            it.copy(
                spaceList = it.spaceList + newItem,
                selectedSpaceId = newItem.id
            )
        }
        checkNextButtonEnabled()
    }

    private fun updateSpaceItemInput(id: Long, input: String) {
        _uiState.update { state ->
            state.copy(spaceList = state.spaceList.map { item ->
                if (item.id == id) item.copy(editInput = input) else item
            })
        }
    }

    private fun saveSpaceItem(id: Long) {
        _uiState.update { state ->
            state.copy(spaceList = state.spaceList.map { item ->
                if (item.id == id) {
                    // [UseCase 적용] 복잡한 계산 및 정리 로직 위임
                    calculateSpaceInfoUseCase(item)
                } else item
            })
        }
        checkNextButtonEnabled()
    }

    private fun removeSpaceItem(id: Long) {
        _uiState.update { state ->
            if (state.spaceList.size <= 1) return@update state
            val nextSelectedId = if (state.selectedSpaceId == id) {
                state.spaceList.find { it.id != id }?.id
            } else state.selectedSpaceId

            state.copy(
                spaceList = state.spaceList.filter { it.id != id },
                selectedSpaceId = nextSelectedId
            )
        }
        checkNextButtonEnabled()
    }

    private fun toggleEditMode(id: Long) {
        _uiState.update { state ->
            state.copy(
                selectedSpaceId = id,
                spaceList = state.spaceList.map { item ->
                    if (item.id == id) {
                        item.copy(isEditing = true, editInput = item.name, inputError = null)
                    } else {
                        item.copy(isEditing = false, inputError = null)
                    }
                }
            )
        }
        checkNextButtonEnabled()
    }

    private fun selectSpace(id: Long) {
        _uiState.update { state ->
            state.copy(
                selectedSpaceId = id,
                spaceList = state.spaceList.map {
                    // 선택 시 다른 항목의 수정 모드는 종료시키는 게 일반적이나, 기획에 따라 유지 가능.
                    // 여기서는 기존 로직 유지 (EditMode 해제)
                    it.copy(isEditing = false, inputError = null)
                }
            )
        }
    }

    private fun addTableItemRow() {
        val selectedId = _uiState.value.selectedSpaceId ?: return
        val newItem = SignUpTableItem(personCount = "", tableCount = "")
        _uiState.update { state ->
            val updatedSpaceList = state.spaceList.map { space ->
                if (space.id == selectedId) space.copy(tableList = space.tableList + newItem) else space
            }
            state.copy(spaceList = updatedSpaceList)
        }
    }

    private fun updateTableItemValue(tableId: Long, nValue: String?, mValue: String?) {
        val selectedId = _uiState.value.selectedSpaceId ?: return
        _uiState.update { state ->
            val updatedSpaceList = state.spaceList.map { space ->
                if (space.id == selectedId) {
                    val newTableList = space.tableList.map { table ->
                        if (table.id == tableId) {
                            table.copy(
                                personCount = nValue ?: table.personCount,
                                tableCount = mValue ?: table.tableCount
                            )
                        } else table
                    }
                    // 단순 좌석 계산 로직은 UI 갱신용이라 VM에 둬도 되지만,
                    // calculateSpaceInfoUseCase가 저장 시점에 최종 계산하므로 여기선 임시 계산만 수행
                    val newSeatCount = newTableList.sumOf { (it.personCount.toIntOrNull() ?: 0) * (it.tableCount.toIntOrNull() ?: 0) }
                    space.copy(tableList = newTableList, seatCount = newSeatCount)
                } else space
            }
            state.copy(spaceList = updatedSpaceList)
        }
    }

    private fun removeTableItemRow(tableId: Long) {
        val selectedId = _uiState.value.selectedSpaceId ?: return
        _uiState.update { state ->
            val targetSpace = state.spaceList.find { it.id == selectedId } ?: return@update state
            if (targetSpace.tableList.size <= 1) return@update state

            val updatedSpaceList = state.spaceList.map { space ->
                if (space.id == selectedId) {
                    val newTableList = space.tableList.filter { it.id != tableId }
                    val newSeatCount = newTableList.sumOf { (it.personCount.toIntOrNull() ?: 0) * (it.tableCount.toIntOrNull() ?: 0) }
                    space.copy(tableList = newTableList, seatCount = newSeatCount)
                } else space
            }
            state.copy(spaceList = updatedSpaceList)
        }
    }

    // --- Step 4 Implementation (유지) ---
    private fun updateOperatingScheduleDays(id: Long, dayIdx: Int) {
        val currentSchedules = _uiState.value.operatingSchedules
        val targetItem = currentSchedules.find { it.id == id } ?: return
        val isOccupiedByOther = currentSchedules.any { item ->
            item.id != id && item.selectedDays.contains(dayIdx)
        }

        if (isOccupiedByOther && !targetItem.selectedDays.contains(dayIdx)) {
            viewModelScope.launch { _event.emit(SignUpEvent.ShowToast("이미 설정된 요일입니다.")) }
            return
        }

        _uiState.update { state ->
            val updatedList = state.operatingSchedules.map { item ->
                if (item.id == id) {
                    val currentDays = item.selectedDays
                    val newDays = if (currentDays.contains(dayIdx)) currentDays - dayIdx else currentDays + dayIdx
                    item.copy(selectedDays = newDays)
                } else item
            }
            state.copy(operatingSchedules = updatedList)
        }
        checkNextButtonEnabled()
    }

    private fun updateOperatingScheduleTime(id: Long, sH: Int, sM: Int, eH: Int, eM: Int) {
        _uiState.update { state ->
            val updatedList = state.operatingSchedules.map { item ->
                if (item.id == id) item.copy(startHour = sH, startMin = sM, endHour = eH, endMin = eM) else item
            }
            state.copy(operatingSchedules = updatedList)
        }
    }

    private fun addStorePhotos(uris: List<Uri>) {
        _uiState.update { state ->
            val currentList = state.storePhotoList
            // [수정] 최대 5장까지만 유지 (take(5))
            val newList = (currentList + uris).distinct().take(5)

            // 리스트가 비어있었는데 추가됐다면, 첫 번째 사진을 자동으로 대표로 설정
            val newRep = if (state.representativePhotoUri == null && newList.isNotEmpty()) {
                newList.first()
            } else {
                state.representativePhotoUri
            }

            state.copy(
                storePhotoList = newList,
                representativePhotoUri = newRep
            )
        }
    }

    private fun removeStorePhoto(uri: Uri) {
        _uiState.update { state ->
            val newList = state.storePhotoList.filter { it != uri }

            // 삭제된 사진이 하필 대표 사진이었다면? -> 남은 사진 중 첫 번째를 대표로 승계
            var newRep = state.representativePhotoUri
            if (uri == state.representativePhotoUri) {
                newRep = newList.firstOrNull()
            }

            state.copy(
                storePhotoList = newList,
                representativePhotoUri = newRep
            )
        }
    }

    private fun setRepresentativePhoto(uri: Uri) {
        // 이미 리스트에 있는 uri인지 확인 후 설정
        if (_uiState.value.storePhotoList.contains(uri)) {
            _uiState.update { it.copy(representativePhotoUri = uri) }
        }
    }

    // ★ [Next Button Check]
    private fun checkNextButtonEnabled() {
        val state = _uiState.value
        val isValid = when (state.currentStep) {
            SignUpStep.STEP_1_BASIC -> {
                state.isEmailVerified && state.isPhoneVerified &&
                        state.password.isNotBlank() && state.passwordError == null &&
                        state.passwordCheck.isNotBlank() && state.passwordCheckError == null &&
                        state.isAllTermsAgreed
            }
            SignUpStep.STEP_2_BUSINESS -> {
                state.repName.isNotBlank() &&
                        state.isBusinessNumVerified &&
                        state.storeName.isNotBlank() &&
                        state.mainAddress.isNotBlank()
            }
            SignUpStep.STEP_3_STORE -> {
                // [요청 사항 반영]
                // 1. 공간 리스트가 비어있지 않아야 함
                // 2. 수정 중인 공간(isEditing == true)이 하나라도 있으면 안 됨
                state.spaceList.isNotEmpty() && state.spaceList.none { it.isEditing }
            }
            SignUpStep.STEP_4_OPERATION -> {
                state.operatingSchedules.isNotEmpty() &&
                        state.operatingSchedules.all { it.selectedDays.isNotEmpty() }
            }
            SignUpStep.STEP_5_PHOTO -> true
            SignUpStep.STEP_6_COMPLETE -> true
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

    private fun handleBackStep() {
        if (_uiState.value.isStoreSearchVisible) {
            _uiState.update { it.copy(isStoreSearchVisible = false) }
            return
        }
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

    enum class TermType(val title: String) {
        AGE("[필수] 만 14세 이상"),
        SERVICE("[필수] 이용약관 동의"),
        PRIVACY_COLLECT("[필수] 개인정보 수집이용 동의"),
        PRIVACY_PROVIDE("[필수] 개인정보 처리방침 동의")
    }

    data class OwnerSignUpUiState(
        val currentStep: SignUpStep = SignUpStep.STEP_2_BUSINESS, //THIS
        val isNextButtonEnabled: Boolean = false,

        //STEP1
        val isAllTermsAgreed: Boolean = false,
        val isAgeVerified: Boolean = false,
        val isServiceVerified: Boolean = false,
        val isPrivacyCollectVerified: Boolean = false,
        val isPrivacyProvideVerified: Boolean = false,
        val openedTermType: TermType? = null,

        val email: String = "",
        val emailError: String? = null,
        val isEmailCodeSent: Boolean = false,
        val isEmailVerified: Boolean = false,
        val emailVerifiedError: String? = null,
        val isEmailVerificationAttempted: Boolean = false,
        val emailTimerText: String? = null,
        val isEmailTimerExpired: Boolean = false,
        val authCode: String = "",

        val password: String = "",
        val passwordError: String? = null,
        val passwordCheck: String = "",
        val passwordCheckError: String? = null,
        val phone: String = "",
        val phoneError: String? = null,
        val isPhoneCodeSent: Boolean = false,
        val isPhoneVerified: Boolean = false,
        val isPhoneVerificationAttempted: Boolean = false,
        val phoneVerifiedError: String? = null,
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

        // Step 3
        val spaceList: List<SpaceItem> = emptyList(),
        val selectedSpaceId: Long? = null,

        // Step 4
        val regularHolidayType: Int = 0,
        val weeklyHolidayDays: Set<Int> = emptySet(),
        val monthlyHolidayWeeks: Set<Int> = emptySet(),
        val monthlyHolidayDays: Set<Int> = emptySet(),

        val isTempHolidayEnabled: Boolean = false,
        val tempHolidayStart: String = "",
        val tempHolidayEnd: String = "",

        val operatingSchedules: List<OperatingScheduleItem> = listOf(
            OperatingScheduleItem(id = 0, startHour = 18, startMin = 0, endHour = 0, endMin = 0)
        ),

        val showWeeklyDayDialog: Boolean = false,
        val showMonthlyWeekDialog: Boolean = false,
        val showMonthlyDayDialog: Boolean = false,
        val showTempHolidayDatePicker: Boolean = false,

        // Step 5
        val storePhotoList: List<Uri> = emptyList(),
        val representativePhotoUri: Uri? = null,
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
        object AddSpaceItemRow : SignUpAction
        data class UpdateSpaceItemInput(val id: Long, val input: String) : SignUpAction
        data class SaveSpaceItem(val id: Long) : SignUpAction
        data class SelectSpace(val id: Long) : SignUpAction
        data class RemoveSpace(val id: Long) : SignUpAction
        data class EditSpace(val id: Long) : SignUpAction

        object AddTableItemRow : SignUpAction
        data class UpdateTableItemN(val tableId: Long, val value: String) : SignUpAction
        data class UpdateTableItemM(val tableId: Long, val value: String) : SignUpAction
        data class RemoveTableItemRow(val tableId: Long) : SignUpAction

        //step4
        data class ToggleRegularHolidayType(val type: Int) : SignUpAction
        data class SetWeeklyDialogVisible(val visible: Boolean) : SignUpAction
        data class SetMonthlyWeekDialogVisible(val visible: Boolean) : SignUpAction
        data class SetMonthlyDayDialogVisible(val visible: Boolean) : SignUpAction
        data class SetTempHolidayDatePickerVisible(val visible: Boolean) : SignUpAction
        data class UpdateWeeklyHolidays(val days: Set<Int>) : SignUpAction
        data class UpdateMonthlyWeeks(val weeks: Set<Int>) : SignUpAction
        data class UpdateMonthlyDays(val days: Set<Int>) : SignUpAction

        object ToggleTempHoliday : SignUpAction
        data class UpdateTempHolidayRange(val start: String, val end: String) : SignUpAction

        object AddOperatingSchedule : SignUpAction
        data class UpdateOperatingDays(val id: Long, val dayIdx: Int) : SignUpAction
        data class UpdateOperatingTime(val id: Long, val startHour: Int, val startMin: Int, val endHour: Int, val endMin: Int) : SignUpAction
        data class RemoveOperatingSchedule(val id: Long) : SignUpAction

        //step5
        data class AddStorePhotos(val uris: List<Uri>) : SignUpAction
        data class RemoveStorePhoto(val uri: Uri) : SignUpAction
        data class SetRepresentativePhoto(val uri: Uri) : SignUpAction

        object OnNextClick : SignUpAction
        object OnBackClick : SignUpAction
    }

    sealed interface SignUpEvent {
        object NavigateBack : SignUpEvent
        object NavigateToHome : SignUpEvent
        data class ShowToast(val message: String) : SignUpEvent
    }
}