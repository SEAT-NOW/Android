package com.gmg.seatnow.presentation.owner.store.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.usecase.auth.OwnerLogoutUseCase
import com.gmg.seatnow.domain.usecase.auth.VerifyOwnerPasswordUseCase
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
    private val verifyOwnerPasswordUseCase: VerifyOwnerPasswordUseCase
) : ViewModel() {

    // UI State (로딩 상태 등)
    data class MyPageUiState(
        val isLoading: Boolean = false,
        val ownerEmail: String = "",       // 이메일
        val ownerPhoneNumber: String = "", // 전화번호
        val isProfileLoaded: Boolean = false, // 로딩 완료 여부
        val checkPassword: String = "",
        val checkPasswordError: String? = null
    )

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState = _uiState.asStateFlow()

    // Event (네비게이션)
    sealed interface MyPageEvent {
        data object NavigateToLogin : MyPageEvent
        data object NavigateToAccountInfo : MyPageEvent
        data object NavigateToEditAccount : MyPageEvent
        data object NavigateToEditSeatConfig : MyPageEvent
        data object NavigateToCheckPassword : MyPageEvent
        data object NavigateToChangePassword : MyPageEvent
    }

    private val _event = MutableSharedFlow<MyPageEvent>()
    val event = _event.asSharedFlow()

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
        }
    }

    private fun fetchOwnerProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // TODO: 실제 API 호출 (Bearer Token은 Repository 레벨에서 Interceptor가 처리한다고 가정)
            // val result = getOwnerProfileUseCase()

            // [Mock Data] API 연동 전 테스트용
            val mockEmail = "test_owner@seatnow.com"
            val mockPhone = "010-1234-5678"

            _uiState.update {
                it.copy(
                    isLoading = false,
                    ownerEmail = mockEmail,
                    ownerPhoneNumber = mockPhone,
                    isProfileLoaded = true
                )
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
}