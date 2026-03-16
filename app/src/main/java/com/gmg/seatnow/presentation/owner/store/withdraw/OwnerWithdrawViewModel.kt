package com.gmg.seatnow.presentation.owner.store.withdraw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.usecase.owner.auth.OwnerWithdrawUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerWithdrawViewModel @Inject constructor(
    private val withdrawUseCase: OwnerWithdrawUseCase
) : ViewModel() {


    private val _uiState = MutableStateFlow(OwnerWithdrawUiState())
    val uiState = _uiState.asStateFlow()


    private val _event = MutableSharedFlow<OwnerWithdrawEvent>()
    val event = _event.asSharedFlow()

    // 3. Action
    fun onAction(action: OwnerWithdrawAction) {
        when (action) {
            is OwnerWithdrawAction.OnToggleConfirm -> {
                _uiState.update { it.copy(isConfirmed = !it.isConfirmed) }
            }
            is OwnerWithdrawAction.OnBusinessNumberChange -> {
                // 숫자만 입력받도록 필터링 (선택 사항)
                val filtered = action.number.filter { it.isDigit() }
                _uiState.update { it.copy(businessNumber = filtered, errorMessage = null) }
            }
            is OwnerWithdrawAction.OnPasswordChange -> {
                _uiState.update { it.copy(password = action.password, errorMessage = null) }
            }
            is OwnerWithdrawAction.OnWithdrawClick -> withdraw()
            is OwnerWithdrawAction.OnBackClick -> {
                viewModelScope.launch { _event.emit(OwnerWithdrawEvent.PopBackStack) }
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
                    _event.emit(OwnerWithdrawEvent.NavigateToLogin)
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

