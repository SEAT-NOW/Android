package com.gmg.seatnow.presentation.owner.store.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val getStoreProfileUseCase: GetStoreProfileUseCase
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

        val representativeName: String = "",
        val businessNumber: String = "",
        val storeName: String = "",
        val storeAddress: String = "",
        val universityName: String = "",
        val licenseFileName: String = "",
        val storeContact: String = "",

        val editStoreContact: String = "",
        val editStoreContactError: String? = null,
        val isStoreContactUpdateSuccess: Boolean = false
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
            is MyPageAction.OnStoreContactConfirmClick -> updateStoreContact()
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
                    // ★ 실패 시 아무것도 하지 않음 -> UI에서 기본값("")을 감지해 "불러오기.." 표시
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
                    // 성공 후 바로 뒤로가기를 원하시면 아래 주석 해제
                    // _event.emit(MyPageEvent.NavigateBack)
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
}