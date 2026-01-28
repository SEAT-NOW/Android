package com.gmg.seatnow.presentation.user.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.usecase.user.GetStoresByHeadCountUseCase
import com.gmg.seatnow.domain.usecase.user.GetStoresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Store>>(emptyList())
    val searchResults: StateFlow<List<Store>> = _searchResults.asStateFlow()

    private var searchJob: Job? = null

    // 필터 설정 (N명 자리찾기 탭에서 넘어올 때 호출)
    fun setHeadCountFilter(count: Int) {
        _activeHeadCount.value = count
    }

    // 필터 해제 (검색바 X 버튼 클릭 시)
    fun clearHeadCountFilter() {
        _activeHeadCount.value = null
    }

    // 지도 데이터 가져오기 (필터 상태에 따라 분기)
    fun fetchStoresInCurrentMap(
        lat: Double,
        lng: Double,
        radius: Double,
        userLat: Double? = null,
        userLng: Double? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val count = _activeHeadCount.value

            val flow = if (count != null) {
                getStoresByHeadCountUseCase(count, null, lat, lng, radius, userLat, userLng)
            } else {
                // 2. 없으면 일반 UseCase (keyword=null)
                getStoresUseCase(null, lat, lng, radius, userLat, userLng)
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

    fun onSearchQueryChanged(
        query: String,
        currentLat: Double,
        currentLng: Double,
        userLat: Double?,
        userLng: Double?
    ) {
        _searchQuery.value = query
        searchJob?.cancel()

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(500L) // Debounce
            performSearchInternal(query, currentLat, currentLng, userLat, userLng)
        }
    }

    private suspend fun performSearchInternal(
        query: String,
        lat: Double,
        lng: Double,
        userLat: Double?,
        userLng: Double?
    ) {
        _isLoading.value = true
        val count = _activeHeadCount.value
        val searchRadius = 5.0 // 검색은 넓게

        val flow = if (count != null) {
            getStoresByHeadCountUseCase(
                headCount = count,
                keyword = query,
                lat = lat, lng = lng, radius = searchRadius,
                userLat = userLat, // ★ 거리 계산용 전달
                userLng = userLng  // ★ 거리 계산용 전달
            )
        } else {
            getStoresUseCase(
                keyword = query,
                lat = lat, lng = lng, radius = searchRadius,
                userLat = userLat, // ★ 거리 계산용 전달
                userLng = userLng  // ★ 거리 계산용 전달
            )
        }

        flow.catch { e ->
            _isLoading.value = false
            e.printStackTrace()
        }.collect { stores ->
            _searchResults.value = stores
            _isLoading.value = false
        }
    }

    // 검색 화면 나갈 때 초기화
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }
}