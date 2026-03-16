package com.gmg.seatnow.presentation.user.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.domain.usecase.user.auth.WithdrawUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserWithdrawViewModel @Inject constructor(
    private val withdrawUserUseCase: WithdrawUserUseCase, // [수정] Mock -> 실제 UseCase 주입
    private val authManager: AuthManager                  // [추가] 토큰 삭제용
) : ViewModel() {



    private val _uiState = MutableStateFlow(UserWithdrawUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<UserWithdrawEvent>()
    val event = _event.asSharedFlow()

    fun onAction(action: UserWithdrawAction) {
        when (action) {
            is UserWithdrawAction.OnToggleConfirm -> {
                _uiState.update { it.copy(isConfirmed = !it.isConfirmed) }
            }
            is UserWithdrawAction.OnWithdrawClick -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true) }

                    // [수정] Clean Architecture로 구현한 실제 API 호출
                    withdrawUserUseCase().onSuccess {
                        authManager.clearTokens() // 성공 시 토큰 및 유저 데이터 초기화
                        _event.emit(UserWithdrawEvent.NavigateToLogin)
                    }.onFailure { error ->
                        val errorMessage = error.message ?: "회원탈퇴에 실패했습니다."
                        _event.emit(UserWithdrawEvent.ShowToast(errorMessage))
                    }

                    _uiState.update { it.copy(isLoading = false) }
                }
            }
            is UserWithdrawAction.OnBackClick -> {
                viewModelScope.launch { _event.emit(UserWithdrawEvent.PopBackStack) }
            }
        }
    }


}