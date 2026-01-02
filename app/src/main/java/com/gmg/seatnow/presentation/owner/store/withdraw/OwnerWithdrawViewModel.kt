package com.gmg.seatnow.presentation.owner.store.withdraw // ✅ 패키지 수정됨

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
        val isLoading: Boolean = false
    )

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
            is WithdrawAction.OnWithdrawClick -> withdraw()
            is WithdrawAction.OnBackClick -> {
                viewModelScope.launch { _event.emit(WithdrawEvent.PopBackStack) }
            }
        }
    }

    private fun withdraw() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            withdrawUseCase()
                .onSuccess { _event.emit(WithdrawEvent.NavigateToLogin) }
                .onFailure { /* 에러 처리 */ }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

// Action
sealed interface WithdrawAction {
    data object OnToggleConfirm : WithdrawAction
    data object OnWithdrawClick : WithdrawAction
    data object OnBackClick : WithdrawAction
}