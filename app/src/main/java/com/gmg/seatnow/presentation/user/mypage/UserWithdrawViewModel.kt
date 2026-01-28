package com.gmg.seatnow.presentation.user.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.domain.usecase.auth.WithdrawUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserWithdrawViewModel @Inject constructor(
    private val withdrawUserUseCase: WithdrawUserUseCase, // [수정] Mock -> 실제 UseCase 주입
    private val authManager: AuthManager                  // [추가] 토큰 삭제용
) : ViewModel() {

    data class UiState(
        val isConfirmed: Boolean = false,
        val isLoading: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<UserWithdrawEvent>()
    val event = _event.asSharedFlow()

    fun onToggleConfirm() {
        _uiState.update { it.copy(isConfirmed = !it.isConfirmed) }
    }

    fun onWithdrawClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // [수정] Clean Architecture로 구현한 실제 API 호출
            withdrawUserUseCase().onSuccess {
                authManager.clearTokens() // 성공 시 토큰 및 유저 데이터 초기화
                _event.emit(UserWithdrawEvent.NavigateToLogin)
            }.onFailure {
                // 필요시 에러 토스트 메시지 등 처리
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onBackClick() {
        viewModelScope.launch { _event.emit(UserWithdrawEvent.PopBackStack) }
    }

    sealed interface UserWithdrawEvent {
        data object NavigateToLogin : UserWithdrawEvent
        data object PopBackStack : UserWithdrawEvent
    }
}