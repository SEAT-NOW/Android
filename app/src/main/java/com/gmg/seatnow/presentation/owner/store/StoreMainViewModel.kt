package com.gmg.seatnow.presentation.owner.store

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class StoreMainViewModel @Inject constructor() : ViewModel() {

    // UI State
    data class StoreMainUiState(
        val currentTab: StoreTab = StoreTab.SEAT_MANAGEMENT
    )

    private val _uiState = MutableStateFlow(StoreMainUiState())
    val uiState = _uiState.asStateFlow()

    // Action (탭 변경만 남음)
    fun onAction(action: StoreMainAction) {
        when (action) {
            is StoreMainAction.ChangeTab -> {
                _uiState.update { it.copy(currentTab = action.tab) }
            }
        }
    }
}

// Action 정의도 축소
sealed interface StoreMainAction {
    data class ChangeTab(val tab: StoreTab) : StoreMainAction
}