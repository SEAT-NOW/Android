package com.gmg.seatnow.presentation.owner.store.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.SignUpTableItem
import com.gmg.seatnow.domain.model.SpaceItem
import com.gmg.seatnow.domain.usecase.owner.auth.ChangeOwnerPasswordUseCase
import com.gmg.seatnow.domain.usecase.owner.auth.GetOwnerAccountUseCase
import com.gmg.seatnow.domain.usecase.owner.store.GetStoreProfileUseCase
import com.gmg.seatnow.domain.usecase.owner.auth.OwnerLogoutUseCase
import com.gmg.seatnow.domain.usecase.owner.store.UpdateStorePhoneUseCase
import com.gmg.seatnow.domain.usecase.owner.auth.VerifyOwnerPasswordUseCase
import com.gmg.seatnow.domain.usecase.common.validation.ValidatePasswordUseCase

// ★ 도메인 UseCase들 주입 (회원가입에서 쓰던 것 재활용 포함)
import com.gmg.seatnow.domain.usecase.owner.seat.GetSeatConfigurationUseCase
import com.gmg.seatnow.domain.usecase.owner.seat.UpdateStoreLayoutUseCase
import com.gmg.seatnow.domain.usecase.common.logic.CalculateSeatCountUseCase

import dagger.hilt.android.lifecycle.HiltViewModel
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

    // ★ Repository 대신 UseCase 3개 주입
    private val getSeatConfigurationUseCase: GetSeatConfigurationUseCase,
    private val updateStoreLayoutUseCase: UpdateStoreLayoutUseCase,
    private val calculateSeatCountUseCase: CalculateSeatCountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<MyPageEvent>()
    val event = _event.asSharedFlow()

    init {
        fetchOwnerProfile()
        fetchStoreProfile()
        fetchSeatConfiguration()
    }

    fun onAction(action: MyPageAction) {
        when (action) {
            is MyPageAction.OnLogoutClick -> logout()
            is MyPageAction.OnAccountInfoClick -> emitEvent(MyPageEvent.NavigateToAccountInfo)
            is MyPageAction.OnEditAccountInfoClick -> emitEvent(MyPageEvent.NavigateToEditAccount)
            is MyPageAction.OnEditSeatConfigClick -> emitEvent(MyPageEvent.NavigateToEditSeatConfig)
            is MyPageAction.OnCheckPasswordClick -> {
                _uiState.update { it.copy(password = it.password.copy(checkPassword = "", checkPasswordError = null)) }
                emitEvent(MyPageEvent.NavigateToCheckPassword)
            }
            is MyPageAction.UpdateCheckPassword -> {
                _uiState.update { it.copy(password = it.password.copy(checkPassword = action.password, checkPasswordError = null)) }
            }
            is MyPageAction.OnCheckPasswordNextClick -> verifyPassword()
            is MyPageAction.UpdateNewPassword -> validateAndUpdateNewPassword(action.password)
            is MyPageAction.UpdateNewPasswordCheck -> validateAndUpdateNewPasswordCheck(action.check)
            is MyPageAction.OnChangePasswordClick -> changePassword()
            is MyPageAction.OnEditStoreInfoClick -> emitEvent(MyPageEvent.NavigateToEditStoreInfo)
            is MyPageAction.OnStoreContactClick -> emitEvent(MyPageEvent.NavigateToEditStoreContact)
            is MyPageAction.UpdateStoreContactInput -> {
                val filtered = action.input.filter { it.isDigit() }
                if (filtered.length <= 11) {
                    _uiState.update {
                        it.copy(storeInfo = it.storeInfo.copy(
                            editStoreContact = filtered,
                            editStoreContactError = null,
                            isStoreContactUpdateSuccess = false
                        ))
                    }
                }
            }
            is MyPageAction.OnStoreContactConfirmClick -> updateStoreContact()
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

    private fun fetchOwnerProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getOwnerAccountUseCase()
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            account = it.account.copy(
                                ownerEmail = data.email,
                                ownerPhoneNumber = data.phoneNumber,
                                isProfileLoaded = true
                            )
                        )
                    }
                }
                .onFailure { _uiState.update { it.copy(isLoading = false) } }
        }
    }

    private fun fetchStoreProfile() {
        viewModelScope.launch {
            getStoreProfileUseCase()
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            storeInfo = it.storeInfo.copy(
                                isStoreLoaded = true,
                                representativeName = data.representativeName,
                                businessNumber = data.businessNumber,
                                storeName = data.storeName,
                                storeAddress = data.address,
                                universityName = data.universityNames?.joinToString(", ") ?: "",
                                licenseFileName = data.businessLicenseFileName ?: "",
                                storeContact = data.storePhone ?: ""
                            )
                        )
                    }
                }
                .onFailure { _uiState.update { it.copy(storeInfo = it.storeInfo.copy(isStoreLoaded = true)) } }
        }
    }

    private fun verifyPassword() {
        val currentPassword = _uiState.value.password.checkPassword
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, password = it.password.copy(checkPasswordError = null)) }
            verifyOwnerPasswordUseCase(currentPassword)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _event.emit(MyPageEvent.NavigateToChangePassword)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, password = it.password.copy(checkPasswordError = error.message ?: "비밀번호 확인에 실패했습니다."))
                    }
                }
        }
    }

    private fun validateAndUpdateNewPassword(password: String) {
        val error = validatePasswordUseCase(password)
        _uiState.update { it.copy(password = it.password.copy(newPassword = password, newPasswordError = error)) }
        validateAndUpdateNewPasswordCheck(_uiState.value.password.newPasswordCheck)
    }

    private fun validateAndUpdateNewPasswordCheck(check: String) {
        val currentPassword = _uiState.value.password.newPassword
        val error = if (check.isNotBlank() && check != currentPassword) "비밀번호가 일치하지 않습니다." else null
        _uiState.update { it.copy(password = it.password.copy(newPasswordCheck = check, newPasswordCheckError = error)) }
        checkChangeButtonEnabled()
    }

    private fun checkChangeButtonEnabled() {
        val state = _uiState.value.password
        val isValid = state.newPassword.isNotBlank() && state.newPasswordError == null &&
                state.newPasswordCheck.isNotBlank() && state.newPasswordCheckError == null &&
                state.newPassword == state.newPasswordCheck
        _uiState.update { it.copy(password = it.password.copy(isChangePasswordButtonEnabled = isValid)) }
    }

    private fun changePassword() {
        val newPassword = _uiState.value.password.newPassword
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            changeOwnerPasswordUseCase(newPassword)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _event.emit(MyPageEvent.ShowToast("비밀번호가 성공적으로 수정되었습니다."))
                    _event.emit(MyPageEvent.NavigateBack)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _event.emit(MyPageEvent.ShowToast(error.message ?: "비밀번호 변경에 실패했습니다."))
                }
        }
    }

    private fun updateStoreContact() {
        val phone = _uiState.value.storeInfo.editStoreContact
        if (phone.length < 9) {
            _uiState.update { it.copy(storeInfo = it.storeInfo.copy(editStoreContactError = "유효한 전화번호를 입력해주세요.")) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            updateStorePhoneUseCase(phone)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            storeInfo = it.storeInfo.copy(
                                isStoreContactUpdateSuccess = true,
                                storeContact = phone
                            )
                        )
                    }
                    _event.emit(MyPageEvent.ShowToast("가게 연락처가 성공적으로 수정되었습니다."))
                    _event.emit(MyPageEvent.NavigateBack)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, storeInfo = it.storeInfo.copy(editStoreContactError = error.message ?: "가게 연락처 수정에 실패했습니다."))
                    }
                }
        }
    }

    private fun fetchSeatConfiguration() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            getSeatConfigurationUseCase()
                .onSuccess { data ->
                    val realCategories = data.categories.filter { category ->
                        category.id != "ALL" && category.name != "전체"
                    }

                    val mappedSpaces = realCategories.map { category ->
                        val tablesInSpace = data.allTables.filter { it.floorId == category.id }
                        val signUpTables = tablesInSpace.map { table ->
                            SignUpTableItem(
                                id = table.id.toLongOrNull() ?: System.currentTimeMillis(),
                                personCount = table.capacityPerTable.toString(),
                                tableCount = table.maxTableCount.toString()
                            )
                        }

                        SpaceItem(
                            id = category.id.toLongOrNull() ?: System.currentTimeMillis(),
                            name = category.name,
                            seatCount = calculateSeatCountUseCase(signUpTables), // ★ UseCase 활용
                            tableList = signUpTables,
                            isEditing = false
                        )
                    }

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

                    val firstSpaceId = finalSpaces.firstOrNull()?.id ?: 0
                    _uiState.update {
                        it.copy(
                            seatConfig = it.seatConfig.copy(
                                spaceList = finalSpaces,
                                selectedSpaceId = firstSpaceId
                            ),
                            isLoading = false
                        )
                    }
                    checkSeatConfigValidity()
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    private fun selectSpace(id: Long) {
        _uiState.update { it.copy(seatConfig = it.seatConfig.copy(selectedSpaceId = id)) }
    }

    private fun editSpace(id: Long) {
        val currentList = _uiState.value.seatConfig.spaceList.map {
            if (it.id == id) it.copy(isEditing = true, editInput = it.name) else it.copy(isEditing = false)
        }
        _uiState.update { it.copy(seatConfig = it.seatConfig.copy(spaceList = currentList, selectedSpaceId = id)) }
    }

    private fun updateSpaceInput(id: Long, input: String) {
        val currentList = _uiState.value.seatConfig.spaceList.map {
            if (it.id == id) it.copy(editInput = input) else it
        }
        _uiState.update { it.copy(seatConfig = it.seatConfig.copy(spaceList = currentList)) }
    }

    private fun saveSpaceItem(id: Long) {
        val currentList = _uiState.value.seatConfig.spaceList.map {
            if (it.id == id) {
                if (it.editInput.isBlank()) return
                it.copy(name = it.editInput, isEditing = false)
            } else it
        }
        _uiState.update { it.copy(seatConfig = it.seatConfig.copy(spaceList = currentList)) }
        checkSeatConfigValidity()
    }

    private fun addSpace() {
        val currentList = _uiState.value.seatConfig.spaceList.toMutableList()
        val newId = -System.currentTimeMillis()
        val newSpace = SpaceItem(
            id = newId,
            name = "",
            tableList = listOf(SignUpTableItem(id = -System.currentTimeMillis() - 1, personCount = "", tableCount = "")),
            isEditing = true,
            editInput = ""
        )
        currentList.add(newSpace)
        _uiState.update { it.copy(seatConfig = it.seatConfig.copy(spaceList = currentList, selectedSpaceId = newId)) }
        checkSeatConfigValidity()
    }

    private fun removeSpace(id: Long) {
        val currentList = _uiState.value.seatConfig.spaceList.toMutableList()
        if (currentList.size > 1) {
            currentList.removeAll { it.id == id }
            val nextSelectedId = if (currentList.isNotEmpty()) currentList.first().id else 0
            _uiState.update { it.copy(seatConfig = it.seatConfig.copy(spaceList = currentList, selectedSpaceId = nextSelectedId)) }
            checkSeatConfigValidity()
        }
    }

    private fun addTableItemRow() {
        val spaceId = _uiState.value.seatConfig.selectedSpaceId
        val currentList = _uiState.value.seatConfig.spaceList.map { space ->
            if (space.id == spaceId) {
                val newTables = space.tableList.toMutableList()
                newTables.add(SignUpTableItem(id = -System.currentTimeMillis(), personCount = "", tableCount = ""))
                space.copy(tableList = newTables, seatCount = calculateSeatCountUseCase(newTables))
            } else space
        }
        _uiState.update { it.copy(seatConfig = it.seatConfig.copy(spaceList = currentList)) }
        checkSeatConfigValidity()
    }

    private fun removeTableItemRow(tableId: Long) {
        val spaceId = _uiState.value.seatConfig.selectedSpaceId
        val currentList = _uiState.value.seatConfig.spaceList.map { space ->
            if (space.id == spaceId) {
                if (space.tableList.size > 1) {
                    val newTables = space.tableList.filter { it.id != tableId }
                    space.copy(tableList = newTables, seatCount = calculateSeatCountUseCase(newTables))
                } else space
            } else space
        }
        _uiState.update { it.copy(seatConfig = it.seatConfig.copy(spaceList = currentList)) }
        checkSeatConfigValidity()
    }

    private fun updateTableItemN(tableId: Long, n: String) {
        if (n.isNotEmpty() && !n.all { it.isDigit() }) return
        val spaceId = _uiState.value.seatConfig.selectedSpaceId
        val currentList = _uiState.value.seatConfig.spaceList.map { space ->
            if (space.id == spaceId) {
                val newTables = space.tableList.map {
                    if (it.id == tableId) it.copy(personCount = n) else it
                }
                space.copy(tableList = newTables, seatCount = calculateSeatCountUseCase(newTables))
            } else space
        }
        _uiState.update { it.copy(seatConfig = it.seatConfig.copy(spaceList = currentList)) }
        checkSeatConfigValidity()
    }

    private fun updateTableItemM(tableId: Long, m: String) {
        if (m.isNotEmpty() && !m.all { it.isDigit() }) return
        val spaceId = _uiState.value.seatConfig.selectedSpaceId
        val currentList = _uiState.value.seatConfig.spaceList.map { space ->
            if (space.id == spaceId) {
                val newTables = space.tableList.map {
                    if (it.id == tableId) it.copy(tableCount = m) else it
                }
                space.copy(tableList = newTables, seatCount = calculateSeatCountUseCase(newTables))
            } else space
        }
        _uiState.update { it.copy(seatConfig = it.seatConfig.copy(spaceList = currentList)) }
        checkSeatConfigValidity()
    }

    private fun checkSeatConfigValidity() {
        val isValid = _uiState.value.seatConfig.spaceList.all { space ->
            space.name.isNotBlank() && !space.isEditing && space.tableList.all { table ->
                table.personCount.isNotEmpty() && (table.personCount.toIntOrNull() ?: 0) > 0 &&
                        table.tableCount.isNotEmpty() && (table.tableCount.toIntOrNull() ?: 0) > 0
            }
        }
        _uiState.update { it.copy(seatConfig = it.seatConfig.copy(isSeatConfigValid = isValid)) }
    }

    private fun saveSeatConfig() {
        val currentSpaceList = _uiState.value.seatConfig.spaceList
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            updateStoreLayoutUseCase(currentSpaceList)
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
                .onFailure { _event.emit(MyPageEvent.NavigateToLogin) }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}