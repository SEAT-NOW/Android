package com.gmg.seatnow.presentation.user.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.usecase.user.GetStoresByHeadCountUseCase
import com.gmg.seatnow.domain.usecase.user.GetStoresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow

@HiltViewModel
class UserHomeViewModel @Inject constructor(
    private val getStoresUseCase: GetStoresUseCase,
    private val getStoresByHeadCountUseCase: GetStoresByHeadCountUseCase
) : ViewModel() {

    private val _storeList = MutableStateFlow<List<Store>>(emptyList())
    val storeList: StateFlow<List<Store>> = _storeList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _activeHeadCount = MutableStateFlow<Int?>(null)
    val activeHeadCount: StateFlow<Int?> = _activeHeadCount.asStateFlow()

    // 필터 설정 (N명 자리찾기 탭에서 넘어올 때 호출)
    fun setHeadCountFilter(count: Int) {
        _activeHeadCount.value = count
    }

    // 필터 해제 (검색바 X 버튼 클릭 시)
    fun clearHeadCountFilter() {
        _activeHeadCount.value = null
    }

    // 지도 데이터 가져오기 (필터 상태에 따라 분기)
    fun fetchStoresInCurrentMap(lat: Double, lng: Double, radius: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            val count = _activeHeadCount.value

            val flow = if (count != null) {
                // UseCase 호출 시 radius 전달
                getStoresByHeadCountUseCase(headCount = count, lat = lat, lng = lng, radius = radius)
            } else {
                getStoresUseCase(lat, lng, radius)
            }

            flow.catch { e ->
                _isLoading.value = false
                e.printStackTrace()
            }.collect { stores ->
                _storeList.value = stores
                _isLoading.value = false
            }
        }
    }
}