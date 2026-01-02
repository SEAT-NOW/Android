package com.gmg.seatnow.presentation.owner.store.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.usecase.auth.OwnerLogoutUseCase
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
    private val logoutUseCase: OwnerLogoutUseCase
) : ViewModel() {

    // UI State (로딩 상태 등)
    data class MyPageUiState(
        val isLoading: Boolean = false
    )

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState = _uiState.asStateFlow()

    // Event (네비게이션)
    sealed interface MyPageEvent {
        data object NavigateToLogin : MyPageEvent
        data object NavigateToAccountInfo : MyPageEvent
    }

    private val _event = MutableSharedFlow<MyPageEvent>()
    val event = _event.asSharedFlow()

    // Action
    fun onAction(action: MyPageAction) {
        when (action) {
            is MyPageAction.OnLogoutClick -> logout()
            is MyPageAction.OnAccountInfoClick -> {
                viewModelScope.launch { _event.emit(MyPageEvent.NavigateToAccountInfo) }
            }
        }
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
}