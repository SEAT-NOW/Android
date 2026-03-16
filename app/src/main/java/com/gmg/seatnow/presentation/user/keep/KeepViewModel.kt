 package com.gmg.seatnow.presentation.user.keep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.domain.usecase.user.mypage.GetKeepStoresUseCase
import com.gmg.seatnow.domain.usecase.user.mypage.ToggleStoreKeepUseCase
import com.gmg.seatnow.domain.usecase.user.auth.CheckDeveloperModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KeepViewModel @Inject constructor(
    private val getKeepStoresUseCase: GetKeepStoresUseCase,
    private val toggleStoreKeepUseCase: ToggleStoreKeepUseCase,
    private val checkDeveloperModeUseCase: CheckDeveloperModeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(KeepUiState())
    val uiState: StateFlow<KeepUiState> = _uiState.asStateFlow()

    init {
        fetchKeepList()
    }

    fun onAction(action: KeepAction) {
        when (action) {
            is KeepAction.FetchKeepList -> fetchKeepList()
            is KeepAction.ToggleKeep -> toggleKeep(action.item)
        }
    }

    private fun fetchKeepList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // 1. Repository에서 실제 킵 목록을 가져옴 (테스터일 시 Reposittory 안에 구현된 가짜 킵 리스트 반환)
            getKeepStoresUseCase().onSuccess { stores ->
                // 2. UI 모델로 변환
                _uiState.update { it.copy(keepList = stores.map { store -> store.toUiModel() }, isLoading = false) }
            }.onFailure {
                // 에러 처리
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun toggleKeep(item: KeepStoreUiModel) {
        // 1. [UI 즉시 반영] 리스트에서 해당 아이템을 바로 제거하여 화면에서 사라지게 함
        val currentList = _uiState.value.keepList.toMutableList()
        currentList.remove(item)
        _uiState.update { it.copy(keepList = currentList) }

        viewModelScope.launch {
            val result = toggleStoreKeepUseCase(item.storeId, false) // isKept = false

            if (result.isFailure) {
                // 실패 시 롤백 (삭제했던 아이템 다시 복구)
                val rollbackList = _uiState.value.keepList.toMutableList()
                rollbackList.add(item)
                _uiState.update { it.copy(keepList = rollbackList) }
                // 필요한 경우 에러 토스트 메시지 전송 로직 추가
            }
        }
    }

    // Helper: Domain Model -> UI Model 변환
    private fun StoreDetail.toUiModel(): KeepStoreUiModel {
        return KeepStoreUiModel(
            storeId = this.id,
            storeName = this.name,
            imageUrl = this.images.firstOrNull() ?: "",
            status = this.status,
            universityName = this.universityInfo,
            availableSeats = this.availableSeatCount,
            totalSeats = this.totalSeatCount,
            isKept = true // 킵 화면에 있다는 건 무조건 true
        )
    }
}