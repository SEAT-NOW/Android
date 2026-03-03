package com.gmg.seatnow.presentation.owner.signup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val requestPhoneAuthCodeUseCase: RequestPhoneAuthCodeUseCase,
    private val requestEmailAuthCodeUseCase: RequestEmailAuthCodeUseCase,
    private val verifyPhoneAuthCodeUseCase: VerifyPhoneAuthCodeUseCase,
    private val verifyEmailAuthCodeUseCase: VerifyEmailAuthCodeUseCase,
    private val verifyBusinessNumberUseCase: VerifyBusinessNumberUseCase,

    // [Store UseCases]
    private val searchStoreUseCase: SearchStoreUseCase,
    private val getNearbyUniversityUseCase: GetNearbyUniversityUseCase,
    private val calculateSeatCountUseCase: CalculateSeatCountUseCase,
    private val checkScheduleCollisionUseCase: CheckScheduleCollisionUseCase,
    private val limitStorePhotosUseCase: LimitStorePhotosUseCase,

    // [Logic UseCases]
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val formatTimerUseCase: FormatTimerUseCase,
    private val calculateSpaceInfoUseCase: CalculateSpaceInfoUseCase,
    private val checkTestAccountUseCase: CheckTestAccountUseCase,
    private val extractNeighborhoodUseCase: ExtractNeighborhoodUseCase,

    private val signUpOwnerUseCase: SignUpOwnerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerSignUpUiState())
    val uiState: StateFlow<OwnerSignUpUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<SignUpEvent>()
    val event: SharedFlow<SignUpEvent> = _event.asSharedFlow()

    private var emailTimerJob: Job? = null
    private var phoneTimerJob: Job? = null

    private val _storeSearchQuery = MutableSharedFlow<String>()

    init {
        viewModelScope.launch {
            @OptIn(FlowPreview::class)
            _storeSearchQuery
                .debounce(500)
                .collect { query ->
                    if (query.isNotBlank()) {
                        searchStoreUseCase(query)
                            .onSuccess { results ->
                                _uiState.update { it.copy(business = it.business.copy(storeSearchResults = results)) }
                            }
                    } else {
                        _uiState.update { it.copy(business = it.business.copy(storeSearchResults = emptyList())) }
                    }
                }
        }
        initializeDefaultSpace()
    }

    private fun initializeDefaultSpace() {
        val defaultSpace = SpaceItem(
            id = System.currentTimeMillis(),
            name = "",
            seatCount = 0,
            isEditing = true,
            editInput = "전체",
            tableList = listOf(SignUpTableItem(personCount = "", tableCount = ""))
        )
        _uiState.update {
            it.copy(
                store = it.store.copy(
                    spaceList = listOf(defaultSpace),
                    selectedSpaceId = defaultSpace.id
                ),
                isNextButtonEnabled = false
            )
        }
    }

    fun onAction(action: SignUpAction) {
        when (action) {
            // Step 1
            is SignUpAction.UpdateEmail -> validateAndUpdateEmail(action.email)
            is SignUpAction.UpdateAuthCode -> _uiState.update { it.copy(basic = it.basic.copy(authCode = action.code)) }
            is SignUpAction.UpdatePassword -> validateAndUpdatePassword(action.password)
            is SignUpAction.UpdatePasswordCheck -> validateAndUpdatePasswordCheck(action.check)
            is SignUpAction.UpdatePhone -> _uiState.update { it.copy(basic = it.basic.copy(phone = action.phone)) }
            is SignUpAction.UpdatePhoneAuthCode -> _uiState.update { it.copy(basic = it.basic.copy(phoneAuthCode = action.code)) }

            is SignUpAction.RequestEmailCode -> requestEmailCode()
            is SignUpAction.VerifyEmailCode -> verifyEmailCode()
            is SignUpAction.RequestPhoneCode -> requestPhoneCode()
            is SignUpAction.VerifyPhoneCode -> verifyPhoneCode()

            is SignUpAction.ToggleAllTerms -> toggleAllTerms(action.isChecked)
            is SignUpAction.ToggleTerm -> toggleSingleTerm(action.termType)
            is SignUpAction.OpenTermDetail -> _uiState.update { it.copy(basic = it.basic.copy(openedTermType = action.termType)) }
            is SignUpAction.CloseTermDetail -> _uiState.update { it.copy(basic = it.basic.copy(openedTermType = null)) }

            // Step 2
            is SignUpAction.UpdateRepName -> { _uiState.update { it.copy(business = it.business.copy(repName = action.name)) } }
            is SignUpAction.UpdateBusinessNum -> {
                if (action.num.length <= 10 && action.num.all { it.isDigit() }) {
                    _uiState.update { it.copy(business = it.business.copy(businessNumber = action.num)) }
                }
            }
            is SignUpAction.VerifyBusinessNum -> verifyBusinessNumber()
            is SignUpAction.OpenStoreSearch -> _uiState.update { it.copy(business = it.business.copy(isStoreSearchVisible = true)) }
            is SignUpAction.CloseStoreSearch -> _uiState.update { it.copy(business = it.business.copy(isStoreSearchVisible = false)) }
            is SignUpAction.SearchStoreQuery -> {
                viewModelScope.launch { _storeSearchQuery.emit(action.query) }
            }
            is SignUpAction.SelectStore -> selectStore(action.store)
            is SignUpAction.UpdateMainAddress -> _uiState.update { it.copy(business = it.business.copy(mainAddress = action.address)) }
            is SignUpAction.UpdateStoreContact -> {
                if (action.phone.length <= 11 && action.phone.all { it.isDigit() }) {
                    _uiState.update { it.copy(business = it.business.copy(storeContact = action.phone)) }
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
                _uiState.update { state ->
                    val newType = if (state.operation.regularHolidayType == action.type) 0 else action.type
                    state.copy(operation = state.operation.copy(regularHolidayType = newType))
                }
            }
            is SignUpAction.UpdateWeeklyHolidays -> _uiState.update { it.copy(operation = it.operation.copy(weeklyHolidayDays = action.days, showWeeklyDayDialog = false)) }
            is SignUpAction.UpdateMonthlyWeeks -> _uiState.update { it.copy(operation = it.operation.copy(monthlyHolidayWeeks = action.weeks, showMonthlyWeekDialog = false)) }
            is SignUpAction.UpdateMonthlyDays -> _uiState.update { it.copy(operation = it.operation.copy(monthlyHolidayDays = action.days, showMonthlyDayDialog = false)) }
            is SignUpAction.ToggleTempHoliday -> _uiState.update { it.copy(operation = it.operation.copy(isTempHolidayEnabled = !it.operation.isTempHolidayEnabled)) }
            is SignUpAction.UpdateTempHolidayRange -> {
                _uiState.update {
                    it.copy(operation = it.operation.copy(tempHolidayStart = action.start, tempHolidayEnd = action.end, showTempHolidayDatePicker = false))
                }
            }
            is SignUpAction.AddOperatingSchedule -> {
                val currentSchedules = _uiState.value.operation.operatingSchedules
                val newId = (currentSchedules.maxOfOrNull { it.id } ?: 0) + 1
                val newItem = OperatingScheduleItem(newId, startHour = 18, startMin = 0, endHour = 0, endMin = 0)
                _uiState.update { it.copy(operation = it.operation.copy(operatingSchedules = currentSchedules + newItem)) }
            }
            is SignUpAction.UpdateOperatingDays -> updateOperatingScheduleDays(action.id, action.dayIdx)
            is SignUpAction.UpdateOperatingTime -> updateOperatingScheduleTime(action.id, action.startHour, action.startMin, action.endHour, action.endMin)
            is SignUpAction.RemoveOperatingSchedule -> {
                _uiState.update { it.copy(operation = it.operation.copy(operatingSchedules = it.operation.operatingSchedules.filter { item -> item.id != action.id })) }
            }

            is SignUpAction.SetWeeklyDialogVisible -> _uiState.update { it.copy(operation = it.operation.copy(showWeeklyDayDialog = action.visible)) }
            is SignUpAction.SetMonthlyWeekDialogVisible -> _uiState.update { it.copy(operation = it.operation.copy(showMonthlyWeekDialog = action.visible)) }
            is SignUpAction.SetMonthlyDayDialogVisible -> _uiState.update { it.copy(operation = it.operation.copy(showMonthlyDayDialog = action.visible)) }
            is SignUpAction.SetTempHolidayDatePickerVisible -> _uiState.update { it.copy(operation = it.operation.copy(showTempHolidayDatePicker = action.visible)) }

            // Step 5
            is SignUpAction.AddStorePhotos -> addStorePhotos(action.uris)
            is SignUpAction.RemoveStorePhoto -> removeStorePhoto(action.uri)
            is SignUpAction.SetRepresentativePhoto -> setRepresentativePhoto(action.uri)

            // Navigation
            is SignUpAction.OnNextClick -> handleNextStep()
            is SignUpAction.OnBackClick -> handleBackStep()
        }

        checkNextButtonEnabled()
    }

    // --- Step 1 Implementation ---

    private fun requestEmailCode() {
        val email = _uiState.value.basic.email
        if (email.isBlank() || _uiState.value.basic.emailError != null) return

        if (checkTestAccountUseCase.isTestEmail(email)) {
            startEmailTimer()
            _uiState.update { it.copy(basic = it.basic.copy(isEmailCodeSent = true, authCode = "", isEmailVerificationAttempted = false, emailVerifiedError = null)) }
            viewModelScope.launch { _event.emit(SignUpEvent.ShowToast("[TEST] 인증번호 123456을 입력하세요.")) }
            return
        }

        viewModelScope.launch {
            requestEmailAuthCodeUseCase(email)
                .onSuccess {
                    startEmailTimer()
                    _uiState.update {
                        it.copy(basic = it.basic.copy(
                            isEmailCodeSent = true,
                            authCode = "",
                            isEmailVerificationAttempted = false,
                            emailVerifiedError = null)) }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(basic = it.basic.copy(emailError = exception.message ?: "인증번호 전송에 실패했습니다.")) } }
        }
    }

    private fun verifyEmailCode() {
        val email = _uiState.value.basic.email
        val code = _uiState.value.basic.authCode
        _uiState.update { it.copy(basic = it.basic.copy(isEmailVerificationAttempted = true)) }

        if (checkTestAccountUseCase.isTestEmail(email) && code == "123456") {
            stopEmailTimer()
            _uiState.update { it.copy(basic = it.basic.copy(isEmailVerified = true, emailTimerText = null, emailVerifiedError = null)) }
            checkNextButtonEnabled()
            return
        }

        stopEmailTimer()
        viewModelScope.launch {
            verifyEmailAuthCodeUseCase(email, code)
                .onSuccess {
                    _uiState.update {
                        it.copy(basic = it.basic.copy(
                            isEmailVerified = true,
                            emailTimerText = null,
                            emailVerifiedError = null)) }
                    checkNextButtonEnabled()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(basic = it.basic.copy(emailVerifiedError = exception.message ?: "인증에 실패했습니다. 다시 시도해주세요.")) } }
        }
    }

    private fun requestPhoneCode() {
        val phone = _uiState.value.basic.phone
        if (phone.length < 10) return
        if (checkTestAccountUseCase.isTestPhone(phone)) {
            startPhoneTimer()
            _uiState.update {
                it.copy(basic = it.basic.copy(
                    isPhoneCodeSent = true,
                    phoneAuthCode = "",
                    isPhoneVerificationAttempted = false,
                    phoneVerifiedError = null
                ))
            }
            viewModelScope.launch {
                _event.emit(SignUpEvent.ShowToast("[TEST] 인증번호 123456을 입력하세요."))
            }
            return
        }

        viewModelScope.launch {
            requestPhoneAuthCodeUseCase(phone)
                .onSuccess {
                    startPhoneTimer()
                    _uiState.update {
                        it.copy(basic = it.basic.copy(
                            isPhoneCodeSent = true,
                            phoneAuthCode = "",
                            isPhoneVerificationAttempted = false,
                            phoneVerifiedError = null
                        ))
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(basic = it.basic.copy(phoneError = exception.message ?: "인증번호 전송에 실패했습니다."))
                    }
                }
        }
    }

    private fun verifyPhoneCode() {
        val phone = _uiState.value.basic.phone
        val code = _uiState.value.basic.phoneAuthCode
        _uiState.update { it.copy(basic = it.basic.copy(isPhoneVerificationAttempted = true)) }
        if (checkTestAccountUseCase.isTestPhone(phone) && code == "123456") {
            stopPhoneTimer()
            _uiState.update {
                it.copy(basic = it.basic.copy(
                    isPhoneVerified = true,
                    phoneTimerText = null,
                    phoneVerifiedError = null
                ))
            }
            checkNextButtonEnabled()
            return
        }

        stopPhoneTimer()
        viewModelScope.launch {
            verifyPhoneAuthCodeUseCase(phone, code)
                .onSuccess {
                    _uiState.update {
                        it.copy(basic = it.basic.copy(
                            isPhoneVerified = true,
                            phoneTimerText = null,
                            phoneVerifiedError = null
                        ))
                    }
                    checkNextButtonEnabled()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(basic = it.basic.copy(phoneVerifiedError = exception.message ?: "인증 번호가 일치하지 않습니다."))
                    }
                }
        }
    }

    private fun validateAndUpdateEmail(email: String) {
        val error = validateEmailUseCase(email)
        _uiState.update { it.copy(basic = it.basic.copy(email = email, emailError = error, isEmailVerified = false, isEmailCodeSent = false, isEmailVerificationAttempted = false)) }
        stopEmailTimer()
    }

    private fun validateAndUpdatePassword(password: String) {
        val error = validatePasswordUseCase(password)
        _uiState.update { it.copy(basic = it.basic.copy(password = password, passwordError = error)) }
        validateAndUpdatePasswordCheck(_uiState.value.basic.passwordCheck)
    }

    private fun validateAndUpdatePasswordCheck(check: String) {
        val currentPassword = _uiState.value.basic.password
        val error = if (check.isNotBlank() && check != currentPassword) "비밀번호가 일치하지 않습니다." else null
        _uiState.update { it.copy(basic = it.basic.copy(passwordCheck = check, passwordCheckError = error)) }
    }

    private fun startEmailTimer() {
        emailTimerJob?.cancel()
        emailTimerJob = viewModelScope.launch {
            var time = 180
            _uiState.update { it.copy(basic = it.basic.copy(isEmailTimerExpired = false)) }
            while (time > 0) {
                val timeString = formatTimerUseCase(time)
                _uiState.update { it.copy(basic = it.basic.copy(emailTimerText = timeString)) }
                delay(1000)
                time--
            }
            _uiState.update { it.copy(basic = it.basic.copy(emailTimerText = "0:00", isEmailTimerExpired = true)) }
        }
    }

    private fun startPhoneTimer() {
        phoneTimerJob?.cancel()
        phoneTimerJob = viewModelScope.launch {
            var time = 180
            _uiState.update { it.copy(basic = it.basic.copy(isPhoneTimerExpired = false)) }
            while (time > 0) {
                val timeString = formatTimerUseCase(time)
                _uiState.update { it.copy(basic = it.basic.copy(phoneTimerText = timeString)) }
                delay(1000)
                time--
            }
            _uiState.update { it.copy(basic = it.basic.copy(phoneTimerText = "0:00", isPhoneTimerExpired = true)) }
        }
    }

    private fun stopEmailTimer() {
        emailTimerJob?.cancel()
        _uiState.update { it.copy(basic = it.basic.copy(emailTimerText = null)) }
    }

    private fun stopPhoneTimer() {
        phoneTimerJob?.cancel()
        _uiState.update { it.copy(basic = it.basic.copy(phoneTimerText = null)) }
    }

    private fun toggleAllTerms(isChecked: Boolean) {
        _uiState.update {
            it.copy(basic = it.basic.copy(
                isAllTermsAgreed = isChecked,
                isAgeVerified = isChecked,
                isServiceVerified = isChecked,
                isPrivacyCollectVerified = isChecked,
                isPrivacyProvideVerified = isChecked
            ))
        }
    }

    private fun toggleSingleTerm(termType: TermType) {
        _uiState.update { state ->
            val basic = state.basic
            val newBasic = when (termType) {
                TermType.AGE -> basic.copy(isAgeVerified = !basic.isAgeVerified)
                TermType.SERVICE -> basic.copy(isServiceVerified = !basic.isServiceVerified)
                TermType.PRIVACY_COLLECT -> basic.copy(isPrivacyCollectVerified = !basic.isPrivacyCollectVerified)
                TermType.PRIVACY_PROVIDE -> basic.copy(isPrivacyProvideVerified = !basic.isPrivacyProvideVerified)
            }
            val allChecked = newBasic.isAgeVerified && newBasic.isServiceVerified &&
                    newBasic.isPrivacyCollectVerified && newBasic.isPrivacyProvideVerified
            state.copy(basic = newBasic.copy(isAllTermsAgreed = allChecked))
        }
    }

    // --- Step 2 Implementation ---

    private fun verifyBusinessNumber() {
        val num = _uiState.value.business.businessNumber
        if (num.length != 10) return

        if (checkTestAccountUseCase.isTestBusinessNum(num)) {
            _uiState.update {
                it.copy(business = it.business.copy(
                    isBusinessNumVerified = true,
                    businessNumberError = null
                ))
            }
            checkNextButtonEnabled()
            viewModelScope.launch {
                _event.emit(SignUpEvent.ShowToast("[TEST] 사업자 인증 통과"))
            }
            return
        }

        viewModelScope.launch {
            verifyBusinessNumberUseCase(num)
                .onSuccess {
                    _uiState.update { it.copy(business = it.business.copy(isBusinessNumVerified = true, businessNumberError = null)) }
                    checkNextButtonEnabled()
                }
                .onFailure { exception -> _uiState.update { it.copy(business = it.business.copy(businessNumberError = exception.message ?: "인증 실패")) } }
        }
    }

    private fun selectStore(store: StoreSearchResult) {
        _uiState.update {
            it.copy(business = it.business.copy(
                storeName = store.placeName,
                mainAddress = store.addressName,
                selectedLatitude = store.latitude,
                selectedLongitude = store.longitude,
                isStoreSearchVisible = false,
                nearbyUniv = "대학 검색 중...",
                isNearbyUnivEnabled = true
            ))
        }

        viewModelScope.launch {
            getNearbyUniversityUseCase(store.latitude, store.longitude)
                .onSuccess { univList ->
                    val resultText = if (univList.isEmpty()) "비대학가" else univList.joinToString(" / ")
                    _uiState.update {
                        it.copy(business = it.business.copy(
                            nearbyUniv = resultText,
                            nearbyUnivList = univList,
                            isNearbyUnivEnabled = false
                        ))
                    }
                    checkNextButtonEnabled()
                }
                .onFailure {
                    _uiState.update {
                        it.copy(business = it.business.copy(
                            nearbyUniv = "대학을 찾을 수 없습니다.",
                            nearbyUnivList = emptyList(),
                            isNearbyUnivEnabled = false
                        ))
                    }
                }
        }
    }

    private fun uploadLicenseImage(uri: Uri, fileName: String) {
        _uiState.update {
            it.copy(business = it.business.copy(
                licenseImageUrl = uri.toString(),
                licenseFileName = fileName
            ))
        }
        checkNextButtonEnabled()
    }

    // --- Step 3 Implementation ---

    private fun addSpaceItemRow() {
        val newItem = SpaceItem(
            id = System.currentTimeMillis(),
            name = "",
            isEditing = true,
            editInput = "",
            tableList = listOf(SignUpTableItem(personCount = "", tableCount = ""))
        )
        _uiState.update {
            it.copy(store = it.store.copy(
                spaceList = it.store.spaceList + newItem,
                selectedSpaceId = newItem.id
            ))
        }
        checkNextButtonEnabled()
    }

    private fun updateSpaceItemInput(id: Long, input: String) {
        _uiState.update { state ->
            state.copy(store = state.store.copy(
                spaceList = state.store.spaceList.map { item ->
                    if (item.id == id) item.copy(editInput = input) else item
                }
            ))
        }
    }

    private fun saveSpaceItem(id: Long) {
        _uiState.update { state ->
            state.copy(store = state.store.copy(
                spaceList = state.store.spaceList.map { item ->
                    if (item.id == id) {
                        calculateSpaceInfoUseCase(item)
                    } else item
                }
            ))
        }
        checkNextButtonEnabled()
    }

    private fun removeSpaceItem(id: Long) {
        _uiState.update { state ->
            if (state.store.spaceList.size <= 1) return@update state
            val nextSelectedId = if (state.store.selectedSpaceId == id) {
                state.store.spaceList.find { it.id != id }?.id
            } else state.store.selectedSpaceId

            state.copy(store = state.store.copy(
                spaceList = state.store.spaceList.filter { it.id != id },
                selectedSpaceId = nextSelectedId
            ))
        }
        checkNextButtonEnabled()
    }

    private fun toggleEditMode(id: Long) {
        _uiState.update { state ->
            state.copy(store = state.store.copy(
                selectedSpaceId = id,
                spaceList = state.store.spaceList.map { item ->
                    if (item.id == id) {
                        item.copy(isEditing = true, editInput = item.name, inputError = null)
                    } else {
                        item.copy(isEditing = false, inputError = null)
                    }
                }
            ))
        }
        checkNextButtonEnabled()
    }

    private fun selectSpace(id: Long) {
        _uiState.update { state ->
            state.copy(store = state.store.copy(
                selectedSpaceId = id,
                spaceList = state.store.spaceList.map {
                    it.copy(isEditing = false, inputError = null)
                }
            ))
        }
    }

    private fun addTableItemRow() {
        val selectedId = _uiState.value.store.selectedSpaceId ?: return
        val newItem = SignUpTableItem(personCount = "", tableCount = "")
        _uiState.update { state ->
            val updatedSpaceList = state.store.spaceList.map { space ->
                if (space.id == selectedId) space.copy(tableList = space.tableList + newItem) else space
            }
            state.copy(store = state.store.copy(spaceList = updatedSpaceList))
        }
    }

    private fun updateTableItemValue(tableId: Long, nValue: String?, mValue: String?) {
        val selectedId = _uiState.value.store.selectedSpaceId ?: return
        _uiState.update { state ->
            val updatedSpaceList = state.store.spaceList.map { space ->
                if (space.id == selectedId) {
                    val newTableList = space.tableList.map { table ->
                        if (table.id == tableId) {
                            table.copy(
                                personCount = nValue ?: table.personCount,
                                tableCount = mValue ?: table.tableCount
                            )
                        } else table
                    }
                    val newSeatCount = calculateSeatCountUseCase(newTableList)
                    space.copy(tableList = newTableList, seatCount = newSeatCount)
                } else space
            }
            state.copy(store = state.store.copy(spaceList = updatedSpaceList))
        }
    }

    private fun removeTableItemRow(tableId: Long) {
        val selectedId = _uiState.value.store.selectedSpaceId ?: return
        _uiState.update { state ->
            val targetSpace = state.store.spaceList.find { it.id == selectedId } ?: return@update state
            if (targetSpace.tableList.size <= 1) return@update state

            val updatedSpaceList = state.store.spaceList.map { space ->
                if (space.id == selectedId) {
                    val newTableList = space.tableList.filter { it.id != tableId }
                    val newSeatCount = calculateSeatCountUseCase(newTableList)
                    space.copy(tableList = newTableList, seatCount = newSeatCount)
                } else space
            }
            state.copy(store = state.store.copy(spaceList = updatedSpaceList))
        }
    }

    // --- Step 4 Implementation ---
    private fun updateOperatingScheduleDays(id: Long, dayIdx: Int) {
        val currentSchedules = _uiState.value.operation.operatingSchedules
        val targetItem = currentSchedules.find { it.id == id } ?: return

        val isOccupiedByOther = checkScheduleCollisionUseCase(currentSchedules, id, dayIdx)

        if (isOccupiedByOther && !targetItem.selectedDays.contains(dayIdx)) {
            viewModelScope.launch { _event.emit(SignUpEvent.ShowToast("이미 설정된 요일입니다.")) }
            return
        }

        _uiState.update { state ->
            val updatedList = state.operation.operatingSchedules.map { item ->
                if (item.id == id) {
                    val currentDays = item.selectedDays
                    val newDays = if (currentDays.contains(dayIdx)) currentDays - dayIdx else currentDays + dayIdx
                    item.copy(selectedDays = newDays)
                } else item
            }
            state.copy(operation = state.operation.copy(operatingSchedules = updatedList))
        }
        checkNextButtonEnabled()
    }

    private fun updateOperatingScheduleTime(id: Long, sH: Int, sM: Int, eH: Int, eM: Int) {
        _uiState.update { state ->
            val updatedList = state.operation.operatingSchedules.map { item ->
                if (item.id == id) item.copy(startHour = sH, startMin = sM, endHour = eH, endMin = eM) else item
            }
            state.copy(operation = state.operation.copy(operatingSchedules = updatedList))
        }
    }

    // --- Step 5 Implementation ---
    private fun addStorePhotos(uris: List<Uri>) {
        _uiState.update { state ->
            val newList = limitStorePhotosUseCase(state.photo.storePhotoList, uris)
            val newRep = if (state.photo.representativePhotoUri == null && newList.isNotEmpty()) {
                newList.first()
            } else {
                state.photo.representativePhotoUri
            }

            state.copy(photo = state.photo.copy(
                storePhotoList = newList,
                representativePhotoUri = newRep
            ))
        }
    }

    private fun removeStorePhoto(uri: Uri) {
        _uiState.update { state ->
            val newList = state.photo.storePhotoList.filter { it != uri }
            var newRep = state.photo.representativePhotoUri
            if (uri == state.photo.representativePhotoUri) {
                newRep = newList.firstOrNull()
            }

            state.copy(photo = state.photo.copy(
                storePhotoList = newList,
                representativePhotoUri = newRep
            ))
        }
    }

    private fun setRepresentativePhoto(uri: Uri) {
        if (_uiState.value.photo.storePhotoList.contains(uri)) {
            _uiState.update { it.copy(photo = it.photo.copy(representativePhotoUri = uri)) }
        }
    }

    private fun checkNextButtonEnabled() {
        val state = _uiState.value
        val isValid = when (state.currentStep) {
            SignUpStep.STEP_1_BASIC -> {
                state.basic.isEmailVerified && state.basic.isPhoneVerified &&
                        state.basic.password.isNotBlank() && state.basic.passwordError == null &&
                        state.basic.passwordCheck.isNotBlank() && state.basic.passwordCheckError == null &&
                        state.basic.isAllTermsAgreed
            }
            SignUpStep.STEP_2_BUSINESS -> {
                state.business.repName.isNotBlank() &&
                        state.business.isBusinessNumVerified &&
                        state.business.storeName.isNotBlank() &&
                        state.business.mainAddress.isNotBlank()
            }
            SignUpStep.STEP_3_STORE -> {
                state.store.spaceList.isNotEmpty() && state.store.spaceList.none { it.isEditing }
            }
            SignUpStep.STEP_4_OPERATION -> {
                state.operation.operatingSchedules.isNotEmpty() &&
                        state.operation.operatingSchedules.all { it.selectedDays.isNotEmpty() }
            }
            SignUpStep.STEP_5_PHOTO -> true
            SignUpStep.STEP_6_COMPLETE -> true
            else -> false
        }
        _uiState.update { it.copy(isNextButtonEnabled = isValid) }
    }

    private fun executeSignUp() {
        viewModelScope.launch {
            val state = _uiState.value
            val domainInfo = mapStateToDomain(state)
            val licenseUri = if(state.business.licenseImageUrl != null) Uri.parse(state.business.licenseImageUrl) else null

            signUpOwnerUseCase(
                info = domainInfo,
                licenseUri = licenseUri,
                storeImageUris = state.photo.storePhotoList,
                representativeUri = state.photo.representativePhotoUri
            )
                .onSuccess { _uiState.update { it.copy(currentStep = SignUpStep.STEP_6_COMPLETE) } }
                .onFailure { e -> _event.emit(SignUpEvent.ShowToast("회원가입 실패: ${e.message}")) }
        }
    }

    private fun mapStateToDomain(state: OwnerSignUpUiState): com.gmg.seatnow.domain.model.OwnerSignUpInfo {
        val account = com.gmg.seatnow.domain.model.AccountInfo(state.basic.email, state.basic.password, state.basic.phone)

        val business = com.gmg.seatnow.domain.model.BusinessInfo(
            representativeName = state.business.repName,
            businessNumber = state.business.businessNumber,
            storeName = state.business.storeName,
            address = state.business.mainAddress,
            neighborhood = extractNeighborhoodUseCase(state.business.mainAddress),
            latitude = state.business.selectedLatitude,
            longitude = state.business.selectedLongitude,
            universityNames = state.business.nearbyUnivList,
            storePhone = state.business.storeContact
        )

        val layout = state.store.spaceList.map { space ->
            com.gmg.seatnow.domain.model.LayoutInfo(
                name = space.name.ifBlank { "기본 홀" },
                tables = space.tableList.map { table ->
                    com.gmg.seatnow.domain.model.TableDetail(
                        tableType = table.personCount.toIntOrNull() ?: 0,
                        tableCount = table.tableCount.toIntOrNull() ?: 0
                    )
                }
            )
        }

        val regularHolidays = when (state.operation.regularHolidayType) {
            1 -> {
                state.operation.weeklyHolidayDays.map { dayIdx ->
                    com.gmg.seatnow.domain.model.RegularHolidayInfo(mapIndexToDayOfWeek(dayIdx), 0)
                }
            }
            2 -> {
                state.operation.monthlyHolidayWeeks.flatMap { week ->
                    state.operation.monthlyHolidayDays.map { day ->
                        com.gmg.seatnow.domain.model.RegularHolidayInfo(mapIndexToDayOfWeek(day), week)
                    }
                }
            }
            else -> emptyList()
        }

        val tempHolidays = if (state.operation.isTempHolidayEnabled && state.operation.tempHolidayStart.isNotBlank()) {
            listOf(com.gmg.seatnow.domain.model.TemporaryHolidayInfo(
                startDate = state.operation.tempHolidayStart.replace("/", "-"),
                endDate = state.operation.tempHolidayEnd.replace("/", "-")
            ))
        } else emptyList()

        val hours = state.operation.operatingSchedules.flatMap { schedule ->
            schedule.selectedDays.map { dayIdx ->
                com.gmg.seatnow.domain.model.OperatingHoursInfo(
                    dayOfWeek = mapIndexToDayOfWeek(dayIdx),
                    startTime = "${schedule.startHour.toString().padStart(2,'0')}:${schedule.startMin.toString().padStart(2,'0')}",
                    endTime = "${schedule.endHour.toString().padStart(2,'0')}:${schedule.endMin.toString().padStart(2,'0')}"
                )
            }
        }

        return com.gmg.seatnow.domain.model.OwnerSignUpInfo(
            account, business, layout,
            com.gmg.seatnow.domain.model.OperationInfo(regularHolidays, tempHolidays, hours)
        )
    }

    private fun mapIndexToDayOfWeek(index: Int): String {
        return when (index) {
            0 -> "SUNDAY"
            1 -> "MONDAY"
            2 -> "TUESDAY"
            3 -> "WEDNESDAY"
            4 -> "THURSDAY"
            5 -> "FRIDAY"
            6 -> "SATURDAY"
            else -> "MONDAY"
        }
    }

    private fun handleNextStep() {
        val currentStep = _uiState.value.currentStep

        if (currentStep == SignUpStep.STEP_5_PHOTO) {
            executeSignUp()
            return
        }

        val nextOrdinal = currentStep.ordinal + 1
        if (nextOrdinal < SignUpStep.entries.size) {
            _uiState.update { it.copy(currentStep = SignUpStep.entries[nextOrdinal]) }
        } else {
            viewModelScope.launch { _event.emit(SignUpEvent.NavigateToHome) }
        }
    }

    private fun handleBackStep() {
        if (_uiState.value.business.isStoreSearchVisible) {
            _uiState.update { it.copy(business = it.business.copy(isStoreSearchVisible = false)) }
            return
        }
        if (_uiState.value.basic.openedTermType != null) {
            _uiState.update { it.copy(basic = it.basic.copy(openedTermType = null)) }
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