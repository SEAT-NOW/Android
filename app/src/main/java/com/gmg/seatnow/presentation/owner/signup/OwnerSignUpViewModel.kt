package com.gmg.seatnow.presentation.owner.signup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.data.repository.ImageRepository
import com.gmg.seatnow.domain.model.StoreSearchResult
import com.gmg.seatnow.domain.usecase.OwnerAuthUseCase
import com.gmg.seatnow.presentation.owner.dataClass.OperatingScheduleItem
import com.gmg.seatnow.presentation.owner.dataClass.SpaceItem
import com.gmg.seatnow.presentation.owner.dataClass.TableItem
import dagger.hilt.android.lifecycle.HiltViewModel
import com.gmg.seatnow.domain.usecase.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OwnerSignUpViewModel @Inject constructor(
    private val authUseCase: OwnerAuthUseCase,
    private val imageRepository: ImageRepository,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val calculateSpaceInfoUseCase: CalculateSpaceInfoUseCase,
    private val formatDateUseCase: FormatDateUseCase,
    private val formatTimerUseCase: FormatTimerUseCase
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
        initializeDefaultSpace()

    }

    private fun initializeDefaultSpace() {
        val defaultSpace = SpaceItem(
            id = System.currentTimeMillis(),
            name = "", // 아직 저장 안됨
            seatCount = 0,
            isEditing = true, // ★ 수정 활성화 상태로 시작
            editInput = "전체", // ★ "전체" 키워드가 미리 입력됨
            tableList = listOf(TableItem(personCount = "", tableCount = ""))
        )
        _uiState.update {
            it.copy(
                spaceList = listOf(defaultSpace),
                selectedSpaceId = defaultSpace.id,
                isNextButtonEnabled = false // ★ 수정 중이므로 다음 버튼 비활성화 (Req 3 자동 적용)
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

            //STEP 3
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

            // [Step 4 Logic]
            // 1. 정기 휴무일 타입 토글 (상호 배타적 선택 보장)
            is SignUpAction.ToggleRegularHolidayType -> {
                _uiState.update {
                    // 이미 선택된 걸 누르면 해제(0), 다른 걸 누르면 그걸로 변경(1 or 2)
                    val newType = if (it.regularHolidayType == action.type) 0 else action.type
                    it.copy(regularHolidayType = newType)
                }
            }

            // 2. 정기 휴무 데이터 업데이트
            is SignUpAction.UpdateWeeklyHolidays -> {
                _uiState.update { it.copy(weeklyHolidayDays = action.days, showWeeklyDayDialog = false) }
            }
            is SignUpAction.UpdateMonthlyWeeks -> {
                _uiState.update { it.copy(monthlyHolidayWeeks = action.weeks, showMonthlyWeekDialog = false) }
            }
            is SignUpAction.UpdateMonthlyDays ->
                _uiState.update { it.copy(monthlyHolidayDays = action.days, showMonthlyDayDialog = false) }
            // 3. 임시 휴무 토글
            is SignUpAction.ToggleTempHoliday -> {
                _uiState.update { it.copy(isTempHolidayEnabled = !it.isTempHolidayEnabled) }
            }

            // ★ [버그 수정] DateRangePicker에서 받은 Long(Millis)을 String으로 변환하여 저장
            is SignUpAction.UpdateTempHolidayRange -> {
                // action.start/end는 UI에서 이미 String으로 변환해서 주거나,
                // 혹은 여기서 변환 로직을 태울 수 있습니다.
                // 이번에는 UI(Screen)에서 Long을 받아서 Action을 호출할 때 String으로 변환해 넘겨주는 방식을 채택합니다.
                _uiState.update {
                    it.copy(
                        tempHolidayStart = action.start,
                        tempHolidayEnd = action.end,
                        showTempHolidayDatePicker = false
                    )
                }
            }

            // 4. 운영 스케줄 관리
            is SignUpAction.AddOperatingSchedule -> {
                val newId = (_uiState.value.operatingSchedules.maxOfOrNull { it.id } ?: 0) + 1
                val newItem = OperatingScheduleItem(newId, startHour = 18, startMin = 0, endHour = 0, endMin = 0)
                _uiState.update { it.copy(operatingSchedules = it.operatingSchedules + newItem) }
            }
            is SignUpAction.UpdateOperatingDays -> updateOperatingScheduleDays(action.id, action.dayIdx)
            is SignUpAction.UpdateOperatingTime -> updateOperatingScheduleTime(action.id, action.startHour, action.startMin, action.endHour, action.endMin)
            is SignUpAction.RemoveOperatingSchedule -> {
                _uiState.update {
                    it.copy(operatingSchedules = it.operatingSchedules.filter { item -> item.id != action.id })
                }
            }

            // 5. 다이얼로그 Visible 제어
            is SignUpAction.SetWeeklyDialogVisible -> _uiState.update { it.copy(showWeeklyDayDialog = action.visible) }
            is SignUpAction.SetMonthlyWeekDialogVisible -> _uiState.update { it.copy(showMonthlyWeekDialog = action.visible) }
            is SignUpAction.SetMonthlyDayDialogVisible -> _uiState.update { it.copy(showMonthlyDayDialog = action.visible) }
            is SignUpAction.SetTempHolidayDatePickerVisible -> _uiState.update { it.copy(showTempHolidayDatePicker = action.visible) }

            //NEXT, BACK
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
                val timeString = formatTimerUseCase(time)

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
        val isValid = validateEmailUseCase(email)
        val error = if (email.isNotBlank() && !isValid) "올바른 이메일 형식이 아닙니다." else null

        _uiState.update { it.copy(email = email, emailError = error, isEmailVerified = false, isEmailCodeSent = false, isEmailVerificationAttempted = false) }
        stopEmailTimer()
    }

    private fun validateAndUpdatePassword(password: String) {
        val isValid = validatePasswordUseCase(password)
        val error = if (password.isNotBlank() && !isValid) "영문, 숫자, 특수문자 포함 8~20자리여야 합니다." else null

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
                storeName = store.placeName,
                mainAddress = store.addressName,
                isStoreSearchVisible = false,
                nearbyUniv = "대학 검색 중...",
                isNearbyUnivEnabled = true // 로딩 중엔 활성화(혹은 스타일 유지)
            )
        }

        viewModelScope.launch {
            authUseCase.getNearbyUniversity(store.latitude, store.longitude)
                .onSuccess { univList ->
                    // ★ [핵심 로직] 리스트가 비어있거나 null인지 확인
                    if (univList.isEmpty()) {
                        _uiState.update {
                            it.copy(
                                nearbyUniv = "근처 대학 없음", // 빈 값 처리
                                isNearbyUnivEnabled = false
                            )
                        }
                    } else {
                        // ★ 여러 개의 대학명을 " / " 로 연결 (예: 명지대학교 / 연세대학교)
                        val joinedUnivString = univList.joinToString(" / ")

                        _uiState.update {
                            it.copy(
                                nearbyUniv = joinedUnivString,
                                isNearbyUnivEnabled = false // 입력 완료 후 비활성화(읽기 전용 느낌)
                            )
                        }
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

    // STEP3
// 1. 공간 추가: 리스트에 '입력 모드'의 빈 공간 아이템 추가
    private fun addSpaceItemRow() {
        val newItem = SpaceItem(
            id = System.currentTimeMillis(),
            name = "",
            isEditing = true, // ★ 생성 시 바로 수정 모드
            editInput = "",
            // 새로 생성된 공간은 빈 테이블 1개를 가진 상태로 시작 (요청사항 반영)
            tableList = listOf(TableItem(personCount = "", tableCount = ""))
        )
        _uiState.update {
            it.copy(
                spaceList = it.spaceList + newItem,
                selectedSpaceId = newItem.id
        ) }
        checkNextButtonEnabled()
    }

    // 2. 공간 입력값 업데이트 (타이핑 시)
    private fun updateSpaceItemInput(id: Long, input: String) {
        _uiState.update { state ->
            state.copy(spaceList = state.spaceList.map { item ->
                if (item.id == id) item.copy(editInput = input) else item
            })
        }
    }

    // 3. 공간 저장 로직: 불완전한 테이블 삭제 & 0석일 때 기본값 리셋
    private fun saveSpaceItem(id: Long) {
        _uiState.update { state ->
            state.copy(spaceList = state.spaceList.map { item ->
                if (item.id == id) {
                    calculateSpaceInfoUseCase(item)
                } else item
            })
        }
        checkNextButtonEnabled()
    }

    // 4. 공간 삭제
    private fun removeSpaceItem(id: Long) {
        _uiState.update { state ->
            // 리스트가 1개 이하라면 삭제하지 않음
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

    // 5. 공간 수정 모드 진입 (연필 아이콘)
    private fun toggleEditMode(id: Long) {
        _uiState.update { state ->
            state.copy(
                selectedSpaceId = id,
                spaceList = state.spaceList.map { item ->
                    if (item.id == id) {
                        item.copy(isEditing = true, editInput = item.name, inputError = null)
                    } else {
                        // 다른 아이템은 수정 모드 종료
                        item.copy(isEditing = false, inputError = null)
                    }
                }
            )
        }
        checkNextButtonEnabled()
    }

    // 6. 공간 선택 (테이블 뷰 갱신용)
    private fun selectSpace(id: Long) {
        _uiState.update { state ->
            state.copy(
                selectedSpaceId = id,
                spaceList = state.spaceList.map {
                    it.copy(
                    isEditing = false, inputError = null
                    )
                }
            )
        }
    }
    // 7. 테이블 행 추가
    private fun addTableItemRow() {
        val selectedId = _uiState.value.selectedSpaceId ?: return
        val newItem = TableItem(personCount = "", tableCount = "")

        _uiState.update { state ->
            val updatedSpaceList = state.spaceList.map { space ->
                if (space.id == selectedId) {
                    space.copy(tableList = space.tableList + newItem)
                } else space
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

    //STEP 4
    private fun updateOperatingScheduleDays(id: Long, dayIdx: Int) {
        val currentSchedules = _uiState.value.operatingSchedules
        val targetItem = currentSchedules.find { it.id == id } ?: return

        // 1. 현재 내가 선택하려는 요일이, '다른' 스케줄 아이템에서 이미 선택되어 있는지 확인
        val isOccupiedByOther = currentSchedules.any { item ->
            item.id != id && item.selectedDays.contains(dayIdx)
        }

        // 2. 이미 선택되어 있고, 현재 아이템에서 '추가'하려고 할 때 (이미 선택된걸 해제하는건 허용)
        if (isOccupiedByOther && !targetItem.selectedDays.contains(dayIdx)) {
            viewModelScope.launch {
                _event.emit(SignUpEvent.ShowToast("이미 설정된 요일입니다."))
            }
            return // 상태 업데이트 하지 않고 종료
        }

        // 3. 정상적인 토글 로직 진행
        _uiState.update { state ->
            val updatedList = state.operatingSchedules.map { item ->
                if (item.id == id) {
                    val currentDays = item.selectedDays
                    val newDays = if (currentDays.contains(dayIdx)) {
                        currentDays - dayIdx
                    } else {
                        currentDays + dayIdx
                    }
                    item.copy(selectedDays = newDays)
                } else item
            }
            state.copy(operatingSchedules = updatedList)
        }

        // 4. 상태 변경 후 유효성 검사 수행
        checkNextButtonEnabled()
    }

    private fun updateOperatingScheduleTime(id: Long, sH: Int, sM: Int, eH: Int, eM: Int) {
        _uiState.update { state ->
            val updatedList = state.operatingSchedules.map { item ->
                if (item.id == id) {
                    item.copy(startHour = sH, startMin = sM, endHour = eH, endMin = eM)
                } else item
            }
            state.copy(operatingSchedules = updatedList)
        }
    }

    fun convertMillisToDateString(millis: Long?): String {
        if (millis == null) return ""
        val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.KOREA)
        return formatter.format(Date(millis))
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
            SignUpStep.STEP_4_OPERATION -> {
                state.operatingSchedules.any {it.selectedDays.isNotEmpty()}
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
        val currentStep: SignUpStep = SignUpStep.STEP_4_OPERATION,
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
        val emaiilVerifedError: String? = null,
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
        val spaceList: List<SpaceItem> = emptyList(),
        val selectedSpaceId: Long? = null,

        // STEP 4
        val regularHolidayType: Int = 0, // 0: 설정안함, 1: 매주, 2: 매월
        val weeklyHolidayDays: Set<Int> = emptySet(), // 매주 휴무 요일 (0:일, 1:월 ~ 6:토) - 다중선택
        val monthlyHolidayWeeks: Set<Int> = emptySet(), // 매월 휴무 주차 (1~5) - 다중선택
        val monthlyHolidayDays: Set<Int> = emptySet(), // 매월 휴무 요일 (0:일 ~ 6:토) - 단일선택 (기획상 언급 없으나 주차 선택시 필요하므로 유지)

        val isTempHolidayEnabled: Boolean = false,
        val tempHolidayStart: String = "", // YYYY/MM/DD
        val tempHolidayEnd: String = "",   // YYYY/MM/DD

        val operatingSchedules: List<OperatingScheduleItem> = listOf(
            // 기본값 18:00 ~ 24:00 (자정은 00:00으로 처리하되 UI 표현 유의)
            OperatingScheduleItem(id = 0, startHour = 18, startMin = 0, endHour = 0, endMin = 0)
        ),

        val showWeeklyDayDialog: Boolean = false,
        val showMonthlyWeekDialog: Boolean = false,
        val showMonthlyDayDialog: Boolean = false,
        val showTempHolidayDatePicker: Boolean = false
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

        // 테이블 조작
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
        data class UpdateOperatingDays(val id: Long, val dayIdx: Int) : SignUpAction // 요일 선택(토글)
        data class UpdateOperatingTime(val id: Long, val startHour: Int, val startMin: Int, val endHour: Int, val endMin: Int) : SignUpAction
        data class RemoveOperatingSchedule(val id: Long) : SignUpAction

        object OnNextClick : SignUpAction
        object OnBackClick : SignUpAction
    }

    sealed interface SignUpEvent {
        object NavigateBack : SignUpEvent
        object NavigateToHome : SignUpEvent
        data class ShowToast(val message: String) : SignUpEvent
    }
}