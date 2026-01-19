package com.gmg.seatnow.presentation.user.seatsearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.usecase.user.GetStoresByHeadCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SeatSearchViewModel @Inject constructor(
    private val getStoresByHeadCountUseCase: GetStoresByHeadCountUseCase
) : ViewModel() {

    enum class SearchStep { INPUT, MAP }

    private val _uiState = MutableStateFlow(SeatSearchUiState(headCount = "4"))
    val uiState: StateFlow<SeatSearchUiState> = _uiState.asStateFlow()

    fun reset() {
        _uiState.value = SeatSearchUiState(
            step = SearchStep.INPUT,
            headCount = "4", // Default 4명
            filteredStoreList = emptyList(),
            isLoading = false
        )
    }

    fun updateHeadCount(count: String) {
        // 1. 빈 값이어도 상태 업데이트 허용 (지울 수 있게 함)
        if (count.isEmpty()) {
            _uiState.value = _uiState.value.copy(headCount = "")
            return
        }

        // 2. 숫자 외 문자 무시
        if (!count.all { it.isDigit() }) return

        // 3. 2자리 초과 시 99로 고정 (기존 유지)
        if (count.length > 2) {
            _uiState.value = _uiState.value.copy(headCount = "99")
            return
        }

        // 4. 숫자로 변환 시도 (0 입력 방지 로직 유지)
        val number = count.toIntOrNull()
        // 0이 들어오면 업데이트 안 함 (기존 값 유지)
        if (number == null || number == 0) return

        // 5. 정상 업데이트
        _uiState.value = _uiState.value.copy(headCount = count)
    }

    // [신규] 입력 완료(포커스 해제) 시 호출되는 함수
    fun finalizeHeadCount() {
        // 만약 비어있다면 "1"로 강제 설정
        if (_uiState.value.headCount.isEmpty()) {
            _uiState.value = _uiState.value.copy(headCount = "1")
        }
    }

    // 인원수 +/- 버튼 조작
    fun adjustHeadCount(amount: Int) {
        val current = _uiState.value.headCount.toIntOrNull() ?: 1
        val next = (current + amount).coerceIn(1, 99)
        _uiState.value = _uiState.value.copy(headCount = next.toString())
    }

    // 검색 실행 (UseCase 호출)
    fun searchStores(lat: Double, lng: Double, radius: Double = 1.0) {
        val count = _uiState.value.headCount.toIntOrNull() ?: 1

        viewModelScope.launch {
            // 변경된 UseCase 서명에 맞춰 radius 전달
            getStoresByHeadCountUseCase(headCount = count, lat = lat, lng = lng, radius = radius)
                .onStart {
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        step = SearchStep.MAP
                    )
                }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    e.printStackTrace()
                }
                .collect { stores ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        filteredStoreList = stores
                    )
                }
        }
    }

    // 입력 화면으로 되돌아가기
    fun goBackToInput() {
        _uiState.value = _uiState.value.copy(step = SearchStep.INPUT, filteredStoreList = emptyList())
    }
}

// UI 상태 관리 (화면 단계, 인원수, 결과 리스트)
data class SeatSearchUiState(
    val step: SeatSearchViewModel.SearchStep = SeatSearchViewModel.SearchStep.INPUT,
    val headCount: String = "4", // 입력 편의를 위해 String 관리, 기본값 2명
    val filteredStoreList: List<Store> = emptyList(),
    val isLoading: Boolean = false
)