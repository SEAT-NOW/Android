package com.gmg.seatnow.presentation.owner.store.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.SignUpTableItem
import com.gmg.seatnow.domain.model.SpaceItem
import com.gmg.seatnow.domain.repository.SeatRepository
import com.gmg.seatnow.domain.usecase.auth.ChangeOwnerPasswordUseCase
import com.gmg.seatnow.domain.usecase.auth.GetOwnerAccountUseCase
import com.gmg.seatnow.domain.usecase.auth.GetStoreProfileUseCase
import com.gmg.seatnow.domain.usecase.auth.OwnerLogoutUseCase
import com.gmg.seatnow.domain.usecase.auth.UpdateStorePhoneUseCase
import com.gmg.seatnow.domain.usecase.auth.VerifyOwnerPasswordUseCase
import com.gmg.seatnow.domain.usecase.logic.ValidatePasswordUseCase
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageViewModel.MyPageEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val logoutUseCase: OwnerLogoutUseCase,
    private val verifyOwnerPasswordUseCase: VerifyOwnerPasswordUseCase,
    private val getOwnerAccountUseCase: GetOwnerAccountUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val changeOwnerPasswordUseCase: ChangeOwnerPasswordUseCase,
    private val updateStorePhoneUseCase: UpdateStorePhoneUseCase,
    private val getStoreProfileUseCase: GetStoreProfileUseCase,
    private val seatRepository: SeatRepository
) : ViewModel() {

    // UI State (로딩 상태 등)
    data class MyPageUiState(
        val isLoading: Boolean = false,
        val ownerEmail: String = "",       // 이메일
        val ownerPhoneNumber: String = "", // 전화번호
        val isProfileLoaded: Boolean = false, // 로딩 완료 여부
        val checkPassword: String = "",
        val checkPasswordError: String? = null,

        val newPassword: String = "",
        val newPasswordError: String? = null,
        val newPasswordCheck: String = "",
        val newPasswordCheckError: String? = null,
        val isChangePasswordButtonEnabled: Boolean = false,

        val isStoreLoaded: Boolean = false,
        val representativeName: String = "",
        val businessNumber: String = "",
        val storeName: String = "",
        val storeAddress: String = "",
        val universityName: String = "",
        val licenseFileName: String = "",
        val storeContact: String = "",

        val editStoreContact: String = "",
        val editStoreContactError: String? = null,
        val isStoreContactUpdateSuccess: Boolean = false,

        val spaceList: List<SpaceItem> = emptyList(),
        val selectedSpaceId: Long = 0, // 현재 선택된 공간 ID
        val isSeatConfigValid: Boolean = false
    )

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<MyPageEvent>()
    val event = _event.asSharedFlow()

    // Event (네비게이션)
    sealed interface MyPageEvent {
        data object NavigateToLogin : MyPageEvent
        data object NavigateToAccountInfo : MyPageEvent
        data object NavigateToEditAccount : MyPageEvent
        data object NavigateToEditSeatConfig : MyPageEvent
        data object NavigateToCheckPassword : MyPageEvent
        data object NavigateToChangePassword : MyPageEvent
        data object NavigateBack : MyPageEvent // ★ 뒤로가기 이벤트
        data class ShowToast(val message: String) : MyPageEvent
        data object NavigateToEditStoreInfo : MyPageEvent
        data object NavigateToEditStoreContact : MyPageEvent
    }

    init {
        fetchOwnerProfile()
        fetchStoreProfile()
        fetchSeatConfiguration()
    }

    // Action
    fun onAction(action: MyPageAction) {
        when (action) {
            is MyPageAction.OnLogoutClick -> logout()

            // "계정 관리" (로그아웃/탈퇴 있는 화면)
            is MyPageAction.OnAccountInfoClick -> {
                emitEvent(MyPageEvent.NavigateToAccountInfo)
            }

            // "계정 정보 수정" (입력 폼)
            is MyPageAction.OnEditAccountInfoClick -> {
                emitEvent(MyPageEvent.NavigateToEditAccount)
            }

            // "좌석 정보 구성 수정"
            is MyPageAction.OnEditSeatConfigClick -> {
                emitEvent(MyPageEvent.NavigateToEditSeatConfig)
            }

            is MyPageAction.OnCheckPasswordClick -> {
                // 화면 이동 전 상태 초기화
                _uiState.update { it.copy(checkPassword = "", checkPasswordError = null) }
                emitEvent(MyPageEvent.NavigateToCheckPassword)
            }

            // ★ 비밀번호 입력 중
            is MyPageAction.UpdateCheckPassword -> {
                _uiState.update { it.copy(checkPassword = action.password, checkPasswordError = null) }
            }

            // ★ '다음' 버튼 클릭 (검증 로직)
            is MyPageAction.OnCheckPasswordNextClick -> verifyPassword()

            is MyPageAction.UpdateNewPassword -> validateAndUpdateNewPassword(action.password)
            is MyPageAction.UpdateNewPasswordCheck -> validateAndUpdateNewPasswordCheck(action.check)
            is MyPageAction.OnChangePasswordClick -> changePassword()

            // ★ [신규] 가게 정보 수정 화면 이동
            is MyPageAction.OnEditStoreInfoClick -> {
                emitEvent(MyPageEvent.NavigateToEditStoreInfo)
            }
            // ★ [신규] 가게 연락처 수정 클릭
            is MyPageAction.OnStoreContactClick -> {
                emitEvent(MyPageEvent.NavigateToEditStoreContact)
            }

            is MyPageAction.OnStoreContactConfirmClick -> updateStoreContact()
            is MyPageAction.UpdateStoreContactInput -> {
                // 숫자만 입력받기 & 에러 초기화 (Step2 로직)
                val filtered = action.input.filter { it.isDigit() }
                if (filtered.length <= 11) { // 11자리 제한
                    _uiState.update {
                        it.copy(
                            editStoreContact = filtered,
                            editStoreContactError = null, // 입력 시 에러 초기화
                            isStoreContactUpdateSuccess = false // 수정 중이면 다시 활성화
                        )
                    }
                }
            }
            is MyPageAction.SelectSpace -> selectSpace(action.id)
            is MyPageAction.EditSpace -> editSpace(action.id)
            is MyPageAction.UpdateSpaceItemInput -> updateSpaceInput(action.id, action.input)
            is MyPageAction.SaveSpaceItem -> saveSpaceItem(action.id)
            is MyPageAction.AddSpaceItemRow -> addSpace()
            is MyPageAction.RemoveSpace -> removeSpace(action.id)

            is MyPageAction.AddTableItemRow -> addTableItemRow()
            is MyPageAction.RemoveTableItemRow -> removeTableItemRow(action.tableId)
            is MyPageAction.UpdateTableItemN -> updateTableItemN(action.tableId, action.n)
            is MyPageAction.UpdateTableItemM -> updateTableItemM(action.tableId, action.m)

            is MyPageAction.OnSaveSeatConfigClick -> saveSeatConfig()
        }
    }

    private fun calculateTotalSeats(tables: List<SignUpTableItem>): Int {
        return tables.sumOf {
            (it.personCount.toIntOrNull() ?: 0) * (it.tableCount.toIntOrNull() ?: 0)
        }
    }

    private fun fetchOwnerProfile() {
        viewModelScope.launch {
            // 로딩 시작 (캐시가 있으면 아주 짧게 보임)
            _uiState.update { it.copy(isLoading = true) }

            getOwnerAccountUseCase()
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            ownerEmail = data.email,       // API 데이터 적용
                            ownerPhoneNumber = data.phoneNumber, // API 데이터 적용
                            isProfileLoaded = true
                        )
                    }
                }
                .onFailure {
                    // 실패 시 로딩만 끔 (필요 시 에러 토스트 처리 가능)
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    private fun fetchStoreProfile() {
        viewModelScope.launch {
            getStoreProfileUseCase()
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            isStoreLoaded = true,
                            representativeName = data.representativeName,
                            businessNumber = data.businessNumber,
                            storeName = data.storeName,
                            storeAddress = data.address,
                            // 리스트를 문자열로 변환 (예: "연세대, 이화여대")
                            universityName = data.universityNames?.joinToString(", ") ?: "",
                            licenseFileName = data.businessLicenseFileName ?: "",
                            storeContact = data.storePhone ?: ""
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isStoreLoaded = true) }
                }
        }
    }

    private fun verifyPassword() {
        val currentPassword = _uiState.value.checkPassword

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, checkPasswordError = null) }

            // 1. API 호출
            verifyOwnerPasswordUseCase(currentPassword)
                .onSuccess {
                    // 2. 성공 (200) -> 비밀번호 변경 화면으로 이동
                    _uiState.update { it.copy(isLoading = false) }
                    _event.emit(MyPageEvent.NavigateToChangePassword)
                }
                .onFailure { error ->
                    // 3. 실패 (400, 404 등) -> 에러 메시지 표시
                    // Repository에서 파싱해준 message("유효하지 않은 비밀번호입니다." 등)를 그대로 씁니다.
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            checkPasswordError = error.message ?: "비밀번호 확인에 실패했습니다."
                        )
                    }
                }
        }
    }

    //비밀번호 변경 로직

    private fun validateAndUpdateNewPassword(password: String) {
        // UseCase를 사용하여 정규식 검사 (영문, 숫자, 특수문자 포함 8~20자리)
        val isValid = validatePasswordUseCase(password)
        val error = if (password.isNotBlank() && !isValid) "영문, 숫자, 특수문자 포함 8~20자리여야 합니다." else null

        _uiState.update { it.copy(newPassword = password, newPasswordError = error) }

        // 비밀번호가 바뀌면 비밀번호 확인 필드도 다시 검사해야 함 (일치 여부 확인)
        validateAndUpdateNewPasswordCheck(_uiState.value.newPasswordCheck)
    }

    private fun validateAndUpdateNewPasswordCheck(check: String) {
        val currentPassword = _uiState.value.newPassword
        val error = if (check.isNotBlank() && check != currentPassword) "비밀번호가 일치하지 않습니다." else null

        _uiState.update {
            it.copy(
                newPasswordCheck = check,
                newPasswordCheckError = error
            )
        }
        checkChangeButtonEnabled()
    }

    private fun checkChangeButtonEnabled() {
        val state = _uiState.value
        // 조건: 비밀번호 유효성 통과(Error == null), 값 존재, 비밀번호 확인 일치
        val isValid = state.newPassword.isNotBlank() && state.newPasswordError == null &&
                state.newPasswordCheck.isNotBlank() && state.newPasswordCheckError == null &&
                state.newPassword == state.newPasswordCheck

        _uiState.update { it.copy(isChangePasswordButtonEnabled = isValid) }
    }

    private fun changePassword() {
        val newPassword = _uiState.value.newPassword

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. 실제 API 호출
            changeOwnerPasswordUseCase(newPassword)
                .onSuccess {
                    // 2. 성공 시
                    _uiState.update { it.copy(isLoading = false) }

                    // 토스트 메시지 띄우고 뒤로가기
                    _event.emit(MyPageEvent.ShowToast("비밀번호가 성공적으로 수정되었습니다."))

                    // 비밀번호 변경 화면 -> 계정 정보 수정 화면으로 이동
                    _event.emit(MyPageEvent.NavigateBack)
                }
                .onFailure { error ->
                    // 3. 실패 시
                    _uiState.update { it.copy(isLoading = false) }

                    // 에러 메시지를 토스트로 띄우거나, 에러 텍스트 필드에 표시할 수 있음
                    // 여기서는 간단히 Toast로 에러 알림
                    val msg = error.message ?: "비밀번호 변경에 실패했습니다."
                    _event.emit(MyPageEvent.ShowToast(msg))
                }
        }
    }

    private fun updateStoreContact() {
        val phone = _uiState.value.editStoreContact

        // 간단한 길이 검증
        if (phone.length < 9) {
            _uiState.update { it.copy(editStoreContactError = "유효한 전화번호를 입력해주세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            updateStorePhoneUseCase(phone)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isStoreContactUpdateSuccess = true, // ★ 성공 시 필드 비활성화
                            storeContact = phone // 화면에 보여주는 기존 데이터도 갱신
                        )
                    }
                    _event.emit(MyPageEvent.ShowToast("가게 연락처가 성공적으로 수정되었습니다."))
                    _event.emit(MyPageEvent.NavigateBack)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            editStoreContactError = error.message ?: "가게 연락처 수정에 실패했습니다."
                        )
                    }
                }
        }
    }

    //좌석 구성 로직

    private fun fetchSeatConfiguration() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            seatRepository.getSeatStatus()
                .onSuccess { data ->
                    // 1. "ALL" (전체) 카테고리 제거
                    // (서버나 레포지토리에서 뷰를 위해 추가한 '전체' 항목은 수정 화면에서 제외)
                    val realCategories = data.categories.filter { category ->
                        category.id != "ALL" && category.name != "전체"
                    }

                    // 2. 서버 데이터 -> UI 편집용 데이터(SpaceItem)로 변환
                    val mappedSpaces = realCategories.map { category ->
                        // 해당 공간(category)에 속한 테이블만 필터링
                        val tablesInSpace = data.allTables.filter { it.floorId == category.id }

                        // TableItem -> SignUpTableItem 변환
                        val signUpTables = tablesInSpace.map { table ->
                            SignUpTableItem(
                                id = table.id.toLongOrNull() ?: System.currentTimeMillis(),
                                personCount = table.capacityPerTable.toString(), // tableType (인원수)
                                tableCount = table.maxTableCount.toString()      // tableCount (개수)
                            )
                        }

                        SpaceItem(
                            id = category.id.toLongOrNull() ?: System.currentTimeMillis(),
                            name = category.name,
                            seatCount = calculateTotalSeats(signUpTables), // 총 좌석 수 계산
                            tableList = signUpTables,
                            isEditing = false
                        )
                    }

                    // 3. 만약 실제 공간이 하나도 없다면 기본값 추가 (신규 가입 직후 등)
                    val finalSpaces = if (mappedSpaces.isEmpty()) {
                        listOf(
                            SpaceItem(
                                id = -System.currentTimeMillis(),
                                name = "홀",
                                seatCount = 0,
                                tableList = listOf(SignUpTableItem(id = -System.currentTimeMillis(), personCount = "", tableCount = ""))
                            )
                        )
                    } else mappedSpaces

                    // 4. State 업데이트
                    val firstSpaceId = finalSpaces.firstOrNull()?.id ?: 0
                    _uiState.update {
                        it.copy(
                            spaceList = finalSpaces,
                            selectedSpaceId = firstSpaceId,
                            isLoading = false
                        )
                    }
                    checkSeatConfigValidity()
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                    // _event.emit(MyPageEvent.ShowToast("좌석 정보를 불러오지 못했습니다."))
                }
        }
    }

    private fun selectSpace(id: Long) {
        _uiState.update { it.copy(selectedSpaceId = id) }
    }

    private fun editSpace(id: Long) {
        val currentList = _uiState.value.spaceList.map {
            if (it.id == id) it.copy(isEditing = true, editInput = it.name) else it.copy(isEditing = false) // 하나만 수정 가능
        }
        _uiState.update { it.copy(spaceList = currentList, selectedSpaceId = id) }
    }

    private fun updateSpaceInput(id: Long, input: String) {
        val currentList = _uiState.value.spaceList.map {
            if (it.id == id) it.copy(editInput = input) else it
        }
        _uiState.update { it.copy(spaceList = currentList) }
    }

    private fun saveSpaceItem(id: Long) {
        val currentList = _uiState.value.spaceList.map {
            if (it.id == id) {
                if (it.editInput.isBlank()) return // 빈 이름 방지
                val totalSeats = calculateTotalSeats(it.tableList)
                it.copy(name = it.editInput, isEditing = false)
            } else it
        }
        _uiState.update { it.copy(spaceList = currentList) }
        checkSeatConfigValidity()
    }

    // ★ [핵심 수정] 공간 추가 시 음수 ID 사용
    private fun addSpace() {
        val currentList = _uiState.value.spaceList.toMutableList()

        // ★ currentTimeMillis의 음수값을 사용하여 신규 아이템임을 표시 (겹칠 확률 극히 낮음)
        val newId = -System.currentTimeMillis()

        val newSpace = SpaceItem(
            id = newId,
            name = "",
            // ★ 테이블 ID도 음수로 생성
            tableList = listOf(SignUpTableItem(id = -System.currentTimeMillis() - 1, personCount = "", tableCount = "")),
            isEditing = true,
            editInput = ""
        )
        currentList.add(newSpace)
        _uiState.update { it.copy(spaceList = currentList, selectedSpaceId = newId) }
        checkSeatConfigValidity()
    }

    private fun removeSpace(id: Long) {
        val currentList = _uiState.value.spaceList.toMutableList()
        if (currentList.size > 1) {
            currentList.removeAll { it.id == id }
            // 삭제 후 선택된 ID 조정
            val nextSelectedId = if (currentList.isNotEmpty()) currentList.first().id else 0
            _uiState.update { it.copy(spaceList = currentList, selectedSpaceId = nextSelectedId) }
            checkSeatConfigValidity()
        }
    }

    // 테이블 로직 (현재 선택된 Space 기준)
    private fun addTableItemRow() {
        val spaceId = _uiState.value.selectedSpaceId
        val currentList = _uiState.value.spaceList.map { space ->
            if (space.id == spaceId) {
                val newTables = space.tableList.toMutableList()
                // ★ 음수 ID 생성
                newTables.add(SignUpTableItem(id = -System.currentTimeMillis(), personCount = "", tableCount = ""))
                space.copy(tableList = newTables, seatCount = calculateTotalSeats(newTables))
            } else space
        }
        _uiState.update { it.copy(spaceList = currentList) }
        checkSeatConfigValidity()
    }

    private fun removeTableItemRow(tableId: Long) {
        val spaceId = _uiState.value.selectedSpaceId
        val currentList = _uiState.value.spaceList.map { space ->
            if (space.id == spaceId) {
                if (space.tableList.size > 1) {
                    val newTables = space.tableList.filter { it.id != tableId }
                    space.copy(tableList = newTables, seatCount = calculateTotalSeats(newTables))
                } else space
            } else space
        }
        _uiState.update { it.copy(spaceList = currentList) }
        checkSeatConfigValidity()
    }

    private fun updateTableItemN(tableId: Long, n: String) {
        if (n.isNotEmpty() && !n.all { it.isDigit() }) return
        val spaceId = _uiState.value.selectedSpaceId
        val currentList = _uiState.value.spaceList.map { space ->
            if (space.id == spaceId) {
                val newTables = space.tableList.map {
                    if (it.id == tableId) it.copy(personCount = n) else it
                }
                space.copy(tableList = newTables, seatCount = calculateTotalSeats(newTables))
            } else space
        }
        _uiState.update { it.copy(spaceList = currentList) }
        checkSeatConfigValidity()
    }

    private fun updateTableItemM(tableId: Long, m: String) {
        if (m.isNotEmpty() && !m.all { it.isDigit() }) return
        val spaceId = _uiState.value.selectedSpaceId
        val currentList = _uiState.value.spaceList.map { space ->
            if (space.id == spaceId) {
                val newTables = space.tableList.map {
                    if (it.id == tableId) it.copy(tableCount = m) else it
                }
                space.copy(tableList = newTables, seatCount = calculateTotalSeats(newTables))
            } else space
        }
        _uiState.update { it.copy(spaceList = currentList) }
        checkSeatConfigValidity()
    }

    private fun checkSeatConfigValidity() {
        // 모든 공간의 이름이 있고, 모든 테이블이 유효해야 함
        val isValid = _uiState.value.spaceList.all { space ->
            space.name.isNotBlank() && !space.isEditing && space.tableList.all { table ->
                table.personCount.isNotEmpty() && (table.personCount.toIntOrNull() ?: 0) > 0 &&
                        table.tableCount.isNotEmpty() && (table.tableCount.toIntOrNull() ?: 0) > 0
            }
        }
        _uiState.update { it.copy(isSeatConfigValid = isValid) }
    }

    private fun saveSeatConfig() {
        val currentSpaceList = _uiState.value.spaceList

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // ★ 여기서 Repository를 호출할 때,
            // Repository 내부에서 "ID < 0"이면 API 전송 시 null로 변환하는 로직이 필요합니다.
            // (ViewModel은 UI용 음수 ID를 그대로 전달합니다)
            seatRepository.updateStoreLayout(currentSpaceList)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _event.emit(MyPageEvent.ShowToast("좌석 정보가 성공적으로 수정되었습니다."))
                    _event.emit(MyPageEvent.NavigateBack)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false) }
                    _event.emit(MyPageEvent.ShowToast("수정에 실패했습니다: ${e.message}"))
                }
        }
    }

    private fun emitEvent(event: MyPageEvent) {
        viewModelScope.launch { _event.emit(event) }
    }

    private fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            logoutUseCase()
                .onSuccess { _event.emit(MyPageEvent.NavigateToLogin) }
                .onFailure { 
                    // 실패해도 일단 로그아웃 처리하거나 에러 메시지 표시
                    _event.emit(MyPageEvent.NavigateToLogin) 
                }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

sealed interface MyPageAction {
    data object OnLogoutClick : MyPageAction
    data object OnAccountInfoClick : MyPageAction
    data object OnEditAccountInfoClick : MyPageAction
    data object OnEditSeatConfigClick : MyPageAction
    data object OnCheckPasswordClick : MyPageAction
    data class UpdateCheckPassword(val password: String) : MyPageAction
    data object OnCheckPasswordNextClick : MyPageAction
    data class UpdateNewPassword(val password: String) : MyPageAction
    data class UpdateNewPasswordCheck(val check: String) : MyPageAction
    data object OnChangePasswordClick : MyPageAction
    data object OnEditStoreInfoClick : MyPageAction
    data object OnStoreContactClick : MyPageAction
    data class UpdateStoreContactInput(val input: String) : MyPageAction
    data object OnStoreContactConfirmClick : MyPageAction
    data class SelectSpace(val id: Long) : MyPageAction
    data class EditSpace(val id: Long) : MyPageAction
    data class UpdateSpaceItemInput(val id: Long, val input: String) : MyPageAction
    data class SaveSpaceItem(val id: Long) : MyPageAction
    data object AddSpaceItemRow : MyPageAction
    data class RemoveSpace(val id: Long) : MyPageAction

    data object AddTableItemRow : MyPageAction
    data class RemoveTableItemRow(val tableId: Long) : MyPageAction
    data class UpdateTableItemN(val tableId: Long, val n: String) : MyPageAction
    data class UpdateTableItemM(val tableId: Long, val m: String) : MyPageAction

    data object OnSaveSeatConfigClick : MyPageAction
}