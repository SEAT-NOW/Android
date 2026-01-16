package com.gmg.seatnow.presentation.owner.store.withdraw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.usecase.auth.OwnerWithdrawUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerWithdrawViewModel @Inject constructor(
    private val withdrawUseCase: OwnerWithdrawUseCase
) : ViewModel() {

    // 1. UI State
    data class WithdrawUiState(
        val isConfirmed: Boolean = false,
        val businessNumber: String = "", // [신규] 사업자 번호
        val password: String = "",       // [신규] 비밀번호
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    ) {
        // [신규] 버튼 활성화 조건: 동의함 && 사업자번호 있음 && 비밀번호 있음 && 로딩 아님
        val isButtonEnabled: Boolean
            get() = isConfirmed && businessNumber.isNotBlank() && password.isNotBlank() && !isLoading
    }

    private val _uiState = MutableStateFlow(WithdrawUiState())
    val uiState = _uiState.asStateFlow()

    // 2. Event
    sealed interface WithdrawEvent {
        data object NavigateToLogin : WithdrawEvent
        data object PopBackStack : WithdrawEvent
    }

    private val _event = MutableSharedFlow<WithdrawEvent>()
    val event = _event.asSharedFlow()

    // 3. Action
    fun onAction(action: WithdrawAction) {
        when (action) {
            is WithdrawAction.OnToggleConfirm -> {
                _uiState.update { it.copy(isConfirmed = !it.isConfirmed) }
            }
            is WithdrawAction.OnBusinessNumberChange -> {
                // 숫자만 입력받도록 필터링 (선택 사항)
                val filtered = action.number.filter { it.isDigit() }
                _uiState.update { it.copy(businessNumber = filtered, errorMessage = null) }
            }
            is WithdrawAction.OnPasswordChange -> {
                _uiState.update { it.copy(password = action.password, errorMessage = null) }
            }
            is WithdrawAction.OnWithdrawClick -> withdraw()
            is WithdrawAction.OnBackClick -> {
                viewModelScope.launch { _event.emit(WithdrawEvent.PopBackStack) }
            }
        }
    }

    private fun withdraw() {
        val currentState = _uiState.value // 현재 상태 스냅샷

        // 로딩 시작
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            // [API 호출] 상태에 저장된 사업자번호와 비밀번호 전달
            withdrawUseCase(
                businessNumber = currentState.businessNumber,
                password = currentState.password
            )
                .onSuccess {
                    // 성공 시 로그인 화면으로 이동
                    _event.emit(WithdrawEvent.NavigateToLogin)
                }
                .onFailure { error ->
                    // ★ [추가] 실패 시 에러 메시지 설정
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "회원탈퇴에 실패했습니다.")
                    }
                }

            // 로딩 종료
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

// Action
sealed interface WithdrawAction {
    data object OnToggleConfirm : WithdrawAction
    data class OnBusinessNumberChange(val number: String) : WithdrawAction
    data class OnPasswordChange(val password: String) : WithdrawAction
    data object OnWithdrawClick : WithdrawAction
    data object OnBackClick : WithdrawAction
}