package com.gmg.seatnow.presentation.owner.store.seat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.TableItem
import com.gmg.seatnow.domain.usecase.store.CalculateSeatDisplayUseCase
import com.gmg.seatnow.domain.usecase.store.GetSeatStatusUseCase
import com.gmg.seatnow.domain.usecase.store.UpdateSeatUsageUseCase
import com.gmg.seatnow.domain.usecase.store.UpdateTableCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeatManagementViewModel @Inject constructor(
    private val updateSeatUsageUseCase: UpdateSeatUsageUseCase,
    private val getSeatStatusUseCase: GetSeatStatusUseCase,
    private val calculateSeatDisplayUseCase: CalculateSeatDisplayUseCase,
    private val updateTableCountUseCase: UpdateTableCountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeatManagementUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<SeatManagementEvent>()
    val event: SharedFlow<SeatManagementEvent> = _event.asSharedFlow()

    // ★ [핵심] 원본 데이터 소스 (Source of Truth)
    private var _allRawTables: List<TableItem> = emptyList()

    init {
        loadData()
    }

    fun onAction(action: SeatManagementAction) {
        when (action) {
            is SeatManagementAction.SelectCategory -> {
                _uiState.update { state -> 
                    state.copy(displayState = state.displayState.copy(selectedCategoryId = action.categoryId)) 
                }
                updateDisplayItems()
            }
            is SeatManagementAction.IncrementTableCount -> processTableUpdate(action.itemId, 1)
            is SeatManagementAction.DecrementTableCount -> processTableUpdate(action.itemId, -1)
            is SeatManagementAction.ToggleDisplayMode -> {
                _uiState.update { state -> 
                    state.copy(displayState = state.displayState.copy(displayMode = action.mode)) 
                }
            }
            is SeatManagementAction.OnUpdateClick -> {
                _uiState.update { state -> 
                    state.copy(displayState = state.displayState.copy(isEditMode = true)) 
                }
                updateDisplayItems()
            }
            is SeatManagementAction.OnSaveClick -> saveSeatData()
        }
    }

    private fun saveSeatData() {
        _uiState.update { state -> 
            state.copy(loadState = state.loadState.copy(isSaving = true)) 
        }

        viewModelScope.launch {
            val allFloorIds = _allRawTables.map { it.floorId }.distinct()
            var isAllSuccess = true

            // 순차 처리 (await)
            for (floorId in allFloorIds) {
                val floorItems = _allRawTables.filter { it.floorId == floorId }
                val result = updateSeatUsageUseCase(floorItems)

                if (result.isFailure) {
                    isAllSuccess = false
                }
            }

            _uiState.update { state -> 
                state.copy(loadState = state.loadState.copy(isSaving = false)) 
            }

            if (isAllSuccess) {
                _uiState.update { state -> 
                    state.copy(displayState = state.displayState.copy(isEditMode = false)) 
                }
                updateDisplayItems()
                _event.emit(SeatManagementEvent.ShowToast("저장되었습니다."))
            } else {
                _event.emit(SeatManagementEvent.ShowToast("일부 데이터 저장에 실패했습니다."))
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { state -> 
                state.copy(loadState = state.loadState.copy(isLoading = true)) 
            }

            getSeatStatusUseCase(forceRefresh = true)
                .onSuccess { data ->
                    _allRawTables = data.allTables

                    _uiState.update { state ->
                        state.copy(
                            seatData = state.seatData.copy(categories = data.categories),
                            loadState = state.loadState.copy(isLoading = false)
                        )
                    }
                    updateDisplayItems()
                }
                .onFailure { e ->
                    // e.message could be handled here
                    _uiState.update { state -> 
                        state.copy(loadState = state.loadState.copy(isLoading = false)) 
                    }
                }
        }
    }

    private fun updateDisplayItems() {
        val countResult = calculateSeatDisplayUseCase(
            CalculateSeatDisplayUseCase.Params(
                allTables = _allRawTables,
                categories = _uiState.value.seatData.categories,
                selectedCategoryId = _uiState.value.displayState.selectedCategoryId,
                isEditMode = _uiState.value.displayState.isEditMode
            )
        )

        _uiState.update { state ->
            state.copy(
                seatData = state.seatData.copy(
                    groupedDisplayItems = countResult.groupedDisplayItems,
                    totalSeatCapacity = countResult.totalCapacity,
                    currentUsedSeats = countResult.usedSeats
                )
            )
        }
    }

    private fun processTableUpdate(itemId: String, delta: Int) {
        _allRawTables = updateTableCountUseCase(_allRawTables, itemId, delta)
        updateDisplayItems()
    }
}