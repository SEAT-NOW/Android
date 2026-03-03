package com.gmg.seatnow.presentation.owner.store.storeManage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.usecase.auth.GetOwnerAccountUseCase
import com.gmg.seatnow.domain.usecase.auth.GetStoreProfileUseCase
import com.gmg.seatnow.domain.usecase.store.FormatMenuCategoryUseCase
import com.gmg.seatnow.domain.usecase.store.FormatStoreDetailUseCase
import com.gmg.seatnow.domain.usecase.store.GetSeatStatusUseCase
import com.gmg.seatnow.domain.usecase.store.GetStoreImagesUseCase
import com.gmg.seatnow.domain.usecase.store.GetStoreMenusUseCase
import com.gmg.seatnow.domain.usecase.store.GetStoreOperationInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreManagementViewModel @Inject constructor(
    private val getOwnerAccountUseCase: GetOwnerAccountUseCase,
    private val getStoreProfileUseCase: GetStoreProfileUseCase,
    private val getSeatStatusUseCase: GetSeatStatusUseCase,
    private val getStoreMenusUseCase: GetStoreMenusUseCase,
    private val getStoreOperationInfoUseCase: GetStoreOperationInfoUseCase,
    private val getStoreImagesUseCase: GetStoreImagesUseCase,
    private val formatStoreDetailUseCase: FormatStoreDetailUseCase,
    private val formatMenuCategoryUseCase: FormatMenuCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreManagementUiState())
    val uiState: StateFlow<StoreManagementUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<StoreManagementEvent>()
    val event: SharedFlow<StoreManagementEvent> = _event.asSharedFlow()

    init {
        loadStoreData()
    }

    fun onAction(action: StoreManagementAction) {
        when (action) {
            is StoreManagementAction.ReloadData -> loadStoreData()
        }
    }

    fun loadStoreData() {
        viewModelScope.launch {
            _uiState.update { state -> 
                state.copy(loadState = state.loadState.copy(isLoading = true)) 
            }

            val ownerAccountDeferred = async { getOwnerAccountUseCase() }
            val storeProfileDeferred = async { getStoreProfileUseCase() }
            val seatStatusDeferred = async { getSeatStatusUseCase(forceRefresh = false) }
            val storeMenusDeferred = async { getStoreMenusUseCase(forceRefresh = true) }
            val operationInfoDeferred = async { getStoreOperationInfoUseCase() }
            val storeImagesDeferred = async { getStoreImagesUseCase() }

            val ownerResult = ownerAccountDeferred.await()
            val storeResult = storeProfileDeferred.await()
            val seatResult = seatStatusDeferred.await()
            val menuResult = storeMenusDeferred.await()
            val operationResult = operationInfoDeferred.await()
            val imagesResult = storeImagesDeferred.await()

            val ownerData = ownerResult.getOrNull()
            val storeData = storeResult.getOrNull()
            val seatData = seatResult.getOrNull()
            val menuData = menuResult.getOrNull() ?: emptyList()
            val operationData = operationResult.getOrNull()
            val imagesData = imagesResult.getOrNull()?.map { it.imageUrl } ?: emptyList()

            // 도메인 캡슐화 관점으로 Format 처리 분리
            val formattedMenuCategories = if (menuResult.isSuccess) {
                formatMenuCategoryUseCase(menuData)
            } else {
                emptyList()
            }

            if (storeData != null) {
                val formattedStoreDetail = formatStoreDetailUseCase(
                    FormatStoreDetailUseCase.Params(
                        storeData = storeData,
                        ownerData = ownerData,
                        seatData = seatData,
                        operationData = operationData,
                        imagesData = imagesData
                    )
                )

                _uiState.update { state ->
                    state.copy(
                        storeData = state.storeData.copy(
                            storeDetail = formattedStoreDetail,
                            menuCategories = formattedMenuCategories
                        )
                    )
                }
            } else {
                storeResult.exceptionOrNull()?.printStackTrace()
                _event.emit(StoreManagementEvent.ShowToast("가게 정보를 불러올 수 없습니다."))
            }

            _uiState.update { state -> 
                state.copy(loadState = state.loadState.copy(isLoading = false)) 
            }
        }
    }
}