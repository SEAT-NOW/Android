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

    private val _relatedUniversities = MutableStateFlow<List<String>>(emptyList())
    val relatedUniversities: StateFlow<List<String>> = _relatedUniversities.asStateFlow()

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
                // 자리찾기: Pair<List<Store>, List<String>> 반환
                getStoresByHeadCountUseCase(
                    headCount = count,
                    keyword = null,
                    lat = lat, lng = lng, radius = radius,
                    userLat = userLat, userLng = userLng
                )
            } else {
                // 일반 지도 조회: Pair 반환
                getStoresUseCase(
                    keyword = null,
                    universityName = null, // ★ 명시적 null 전달
                    lat = lat, lng = lng, radius = radius,
                    userLat = userLat, userLng = userLng
                )
            }

            flow.catch { e ->
                _isLoading.value = false
                e.printStackTrace()
            }.collect { (stores, _) -> // ★ [수정] Pair 분해하여 stores만 사용
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
            _relatedUniversities.value = emptyList()
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
        val searchRadius = 5.0

        // UseCase 호출 (이제 Pair<List<Store>, List<String>>을 반환함)
        val flow = if (count != null) {
            getStoresByHeadCountUseCase(
                headCount = count,
                keyword = query,
                lat = lat, lng = lng, radius = searchRadius,
                userLat = userLat,
                userLng = userLng
            )
        } else {
            getStoresUseCase(
                keyword = query,
                lat = lat, lng = lng, radius = searchRadius,
                userLat = userLat,
                userLng = userLng
            )
        }

        flow.catch { e ->
            _isLoading.value = false
            e.printStackTrace()
        }.collect { (stores, universities) -> // ★ 구조 분해 선언 (Destructuring)

            // 1. 가게 목록 업데이트
            _searchResults.value = stores

            // 2. 관련 대학 목록 업데이트
            _relatedUniversities.value = universities

            _isLoading.value = false
        }
    }

    fun fetchStoresByUniversity(
        uniName: String,
        lat: Double,
        lng: Double,
        radius: Double,
        userLat: Double?,
        userLng: Double?,
        onResultLoaded: (Store?) -> Unit // 첫 번째 결과 반환용 콜백
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            // ★ universityName 파라미터 사용
            val flow = getStoresUseCase(
                keyword = null,           // 일반 검색어는 비움
                universityName = uniName, // 대학명 파라미터 사용
                lat = lat,
                lng = lng,
                radius = radius,
                userLat = userLat,
                userLng = userLng
            )

            flow.catch { e ->
                _isLoading.value = false
                e.printStackTrace()
                onResultLoaded(null)
            }.collect { (stores, _) ->
                _storeList.value = stores // 지도에 핀 갱신
                _isLoading.value = false

                // 검색된 첫 번째 가게 정보를 콜백으로 전달 (지도 이동을 위해)
                onResultLoaded(stores.firstOrNull())
            }
        }
    }

    // 검색 화면 나갈 때 초기화
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _relatedUniversities.value = emptyList()
    }
}