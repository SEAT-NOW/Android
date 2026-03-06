package com.gmg.seatnow.presentation.user.term

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.domain.usecase.auth.SaveGuestTermsUseCase
import com.gmg.seatnow.domain.usecase.auth.SaveKakaoTermsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserTermsViewModel @Inject constructor(
    private val saveGuestTermsUseCase: SaveGuestTermsUseCase,
    private val saveKakaoTermsUseCase: SaveKakaoTermsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserTermsUiState())
    val uiState: StateFlow<UserTermsUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<UserTermsEvent>()
    val event = _event.asSharedFlow()

    fun onAction(action: UserTermsAction) {
        when (action) {
            is UserTermsAction.OnToggleAll -> toggleAll(action.isChecked)
            is UserTermsAction.OnToggleTerm -> toggleTerm(action.type)
            is UserTermsAction.OnOpenDetail -> openDetail(action.type)
            is UserTermsAction.OnCloseDetail -> closeDetail()
            is UserTermsAction.OnSaveTermsAgreement -> saveTermsAgreement(action.isGuest)
        }
    }

    private fun saveTermsAgreement(isGuest: Boolean) {
        viewModelScope.launch {
            if (isGuest) {
                saveGuestTermsUseCase(true)
            } else {
                saveKakaoTermsUseCase(true)
            }
            _event.emit(UserTermsEvent.NavigateToMain)
        }
    }

    private fun toggleAll(isChecked: Boolean) {
        _uiState.update {
            it.copy(
                isAllChecked = isChecked,
                isAgeChecked = isChecked,
                isServiceChecked = isChecked,
                isPrivacyCollectChecked = isChecked,
                isPrivacyProvideChecked = isChecked,
                isLocationChecked = isChecked
            )
        }
    }

    private fun toggleTerm(type: UserTermType) {
        _uiState.update { state ->
            val newState = when(type) {
                UserTermType.AGE -> state.copy(isAgeChecked = !state.isAgeChecked)
                UserTermType.SERVICE -> state.copy(isServiceChecked = !state.isServiceChecked)
                UserTermType.PRIVACY_COLLECT -> state.copy(isPrivacyCollectChecked = !state.isPrivacyCollectChecked)
                UserTermType.PRIVACY_PROVIDE -> state.copy(isPrivacyProvideChecked = !state.isPrivacyProvideChecked)
                UserTermType.LOCATION -> state.copy(isLocationChecked = !state.isLocationChecked)
            }
            val allChecked = newState.isAgeChecked && newState.isServiceChecked &&
                    newState.isPrivacyCollectChecked && newState.isPrivacyProvideChecked &&
                    newState.isLocationChecked
            newState.copy(isAllChecked = allChecked)
        }
    }

    private fun openDetail(type: UserTermType) {
        _uiState.update { it.copy(openedTermType = type) }
    }

    private fun closeDetail() {
        _uiState.update { it.copy(openedTermType = null) }
    }
}