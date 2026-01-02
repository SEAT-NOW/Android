package com.gmg.seatnow.presentation.owner.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.usecase.auth.OwnerLogoutUseCase
import com.gmg.seatnow.domain.usecase.auth.OwnerWithdrawUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreMainViewModel @Inject constructor(
    private val logoutUseCase: OwnerLogoutUseCase,
    private val withdrawUseCase: OwnerWithdrawUseCase
) : ViewModel() {

    // 1. UI State
    data class StoreMainUiState(
        val currentTab: StoreTab = StoreTab.SEAT_MANAGEMENT,
        val isLoading: Boolean = false
    )

    private val _uiState = MutableStateFlow(StoreMainUiState())
    val uiState = _uiState.asStateFlow()

    // 2. Event (네비게이션 등 일회성 효과)
    sealed interface StoreMainEvent {
        data object NavigateToLogin : StoreMainEvent
        data object NavigateToAccountInfo : StoreMainEvent // ✅ [추가] 계정 정보 화면 이동 이벤트
    }

    private val _event = MutableSharedFlow<StoreMainEvent>()
    val event = _event.asSharedFlow()

    // 3. Action (UI 상호작용)
    fun onAction(action: StoreMainAction) {
        when (action) {
            is StoreMainAction.ChangeTab -> {
                _uiState.update { it.copy(currentTab = action.tab) }
            }
            is StoreMainAction.OnLogoutClick -> logout()
            is StoreMainAction.OnWithdrawClick -> withdraw()

            // ✅ [추가] 여기가 누락되었던 부분입니다. Action을 받아 Event를 발생시킵니다.
            is StoreMainAction.NavigateToAccountInfo -> {
                viewModelScope.launch {
                    _event.emit(StoreMainEvent.NavigateToAccountInfo)
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            logoutUseCase()
                .onSuccess { _event.emit(StoreMainEvent.NavigateToLogin) }
                .onFailure { /* 에러 처리 */ }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun withdraw() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            withdrawUseCase()
                .onSuccess { _event.emit(StoreMainEvent.NavigateToLogin) }
                .onFailure { /* 에러 처리 */ }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

// Action 정의
sealed interface StoreMainAction {
    data class ChangeTab(val tab: StoreTab) : StoreMainAction
    data object OnLogoutClick : StoreMainAction
    data object OnWithdrawClick : StoreMainAction
    data object NavigateToAccountInfo : StoreMainAction // ✅ [추가]
}