package com.gmg.seatnow.presentation.user.keep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.domain.usecase.store.GetKeepStoresUseCase
import com.gmg.seatnow.domain.usecase.store.ToggleStoreKeepUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KeepViewModel @Inject constructor(
    private val getKeepStoresUseCase: GetKeepStoresUseCase,   // ★ 추가
    private val toggleStoreKeepUseCase: ToggleStoreKeepUseCase // ★ 추가
) : ViewModel() {

    // UI에서 사용할 데이터 모델 리스트
    private val _keepList = MutableStateFlow<List<KeepStoreUiModel>>(emptyList())
    val keepList: StateFlow<List<KeepStoreUiModel>> = _keepList.asStateFlow()

    init {
        fetchKeepList()
    }

    fun fetchKeepList() {
        viewModelScope.launch {
            // 1. Repository에서 실제 킵 목록을 가져옴
            getKeepStoresUseCase().onSuccess { stores ->
                // 2. UI 모델로 변환
                _keepList.value = stores.map { it.toUiModel() }
            }.onFailure {
                // 에러 처리
            }
        }
    }

    fun toggleKeep(item: KeepStoreUiModel) {
        val currentList = _keepList.value.toMutableList()

        // 1. [Optimistic Update] 리스트에서 즉시 제거
        currentList.remove(item)
        _keepList.value = currentList

        // 2. [API Sync] Repository에 변경 사항 전송 (그래야 상세화면 가도 반영됨)
        viewModelScope.launch {
            val result = toggleStoreKeepUseCase(item.storeId, false) // 킵 해제(false)

            if (result.isFailure) {
                // 실패 시 롤백 로직 (생략 가능하지만 정석은 다시 추가해주는 것)
                fetchKeepList()
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