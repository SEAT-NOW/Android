package com.gmg.seatnow.presentation.user.keep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.data.local.AuthManager
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
    private val toggleStoreKeepUseCase: ToggleStoreKeepUseCase, // ★ 추가
    private val authManager: AuthManager
) : ViewModel() {

    // UI에서 사용할 데이터 모델 리스트
    private val _keepList = MutableStateFlow<List<KeepStoreUiModel>>(emptyList())
    val keepList: StateFlow<List<KeepStoreUiModel>> = _keepList.asStateFlow()

    init {
        fetchKeepList()
    }

    fun fetchKeepList() {
        // ★ [분기] 테스터라면 AuthManager의 가짜 리스트 사용
        if (authManager.isTester()) {
            val fakeList = authManager.getFakeKeepList()
            _keepList.value = fakeList.map { it.toUiModel() }
            return
        }

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
        // 1. [UI 즉시 반영] 리스트에서 해당 아이템을 바로 제거하여 화면에서 사라지게 함
        val currentList = _keepList.value.toMutableList()
        currentList.remove(item)
        _keepList.value = currentList

        // 2. [데이터 반영] 테스터 vs 일반 유저 분기
        if (authManager.isTester()) {
            // 테스터: 메모리 상의 가짜 리스트에서 삭제
            authManager.removeFakeKeep(item.storeId)
        } else {
            // 일반 유저: 실제 서버 API 호출 (킵 해제)
            viewModelScope.launch {
                val result = toggleStoreKeepUseCase(item.storeId, false) // isKept = false

                if (result.isFailure) {
                    // 실패 시 롤백 (삭제했던 아이템 다시 복구)
                    val rollbackList = _keepList.value.toMutableList()
                    rollbackList.add(item)
                    _keepList.value = rollbackList
                    // 필요한 경우 에러 토스트 메시지 전송 로직 추가
                }
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