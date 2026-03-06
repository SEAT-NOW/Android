package com.gmg.seatnow.presentation.user.seatsearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.usecase.user.AdjustHeadCountUseCase
import com.gmg.seatnow.domain.usecase.user.ValidateHeadCountUseCase
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
class SeatSearchViewModel @Inject constructor(
    private val validateHeadCountUseCase: ValidateHeadCountUseCase,
    private val adjustHeadCountUseCase: AdjustHeadCountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeatSearchUiState(headCount = "4"))
    val uiState: StateFlow<SeatSearchUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<SeatSearchEvent>()
    val event = _event.asSharedFlow()

    fun onAction(action: SeatSearchAction) {
        when (action) {
            is SeatSearchAction.OnHeadCountChanged -> updateHeadCount(action.count)
            is SeatSearchAction.OnHeadCountFinalize -> finalizeHeadCount()
            is SeatSearchAction.OnAdjustHeadCount -> adjustHeadCount(action.amount)
            is SeatSearchAction.OnSearchClick -> performSearch()
        }
    }

    private fun updateHeadCount(count: String) {
        val validated = validateHeadCountUseCase(count)
        if (validated != null) {
            _uiState.update { it.copy(headCount = validated) }
        }
    }

    private fun finalizeHeadCount() {
        if (_uiState.value.headCount.isEmpty()) {
            _uiState.update { it.copy(headCount = "1") }
        }
    }

    private fun adjustHeadCount(amount: Int) {
        val next = adjustHeadCountUseCase(_uiState.value.headCount, amount)
        _uiState.update { it.copy(headCount = next) }
    }

    private fun performSearch() {
        val count = _uiState.value.headCount.toIntOrNull() ?: 0
        if (count > 0) {
            viewModelScope.launch {
                _event.emit(SeatSearchEvent.OnSearchConfirmed(count))
            }
        }
    }
}