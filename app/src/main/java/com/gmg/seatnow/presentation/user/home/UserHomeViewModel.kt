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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow

@HiltViewModel
class UserHomeViewModel @Inject constructor(
    private val getStoresUseCase: GetStoresUseCase,
    private val getStoresByHeadCountUseCase: GetStoresByHeadCountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserHomeUiState())
    val uiState: StateFlow<UserHomeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    // For backward compatibility until UI is refactored:
    val storeList: StateFlow<List<Store>> = MutableStateFlow(emptyList()) 
    val isLoading: StateFlow<Boolean> = MutableStateFlow(false)
    val activeHeadCount: StateFlow<Int?> = MutableStateFlow(null)
    val searchQuery: StateFlow<String> = MutableStateFlow("")
    val searchResults: StateFlow<List<Store>> = MutableStateFlow(emptyList())
    val relatedUniversities: StateFlow<List<String>> = MutableStateFlow(emptyList())
    
    init {
        // bridge old individual states for easy migration
        viewModelScope.launch {
            _uiState.collect { state ->
                (storeList as MutableStateFlow).value = state.storeList
                (isLoading as MutableStateFlow).value = state.isLoading
                (activeHeadCount as MutableStateFlow).value = state.activeHeadCount
                (searchQuery as MutableStateFlow).value = state.searchQuery
                (searchResults as MutableStateFlow).value = state.searchResults
                (relatedUniversities as MutableStateFlow).value = state.relatedUniversities
            }
        }
    }

    fun onAction(action: UserHomeAction) {
        when (action) {
            is UserHomeAction.SetHeadCountFilter -> setHeadCountFilter(action.count)
            is UserHomeAction.ClearHeadCountFilter -> clearHeadCountFilter()
            is UserHomeAction.FetchStoresInCurrentMap -> fetchStoresInCurrentMap(
                action.lat, action.lng, action.radius, action.userLat, action.userLng
            )
            is UserHomeAction.OnSearchQueryChanged -> onSearchQueryChanged(
                action.query, action.currentLat, action.currentLng, action.userLat, action.userLng
            )
            is UserHomeAction.FetchStoresByUniversity -> fetchStoresByUniversity(
                action.uniName, action.lat, action.lng, action.radius, action.userLat, action.userLng, action.onResultLoaded
            )
            is UserHomeAction.ClearSearch -> clearSearch()
        }
    }

    // 필터 설정 (N명 자리찾기 탭에서 넘어올 때 호출)
    private fun setHeadCountFilter(count: Int) {
        _uiState.update { it.copy(activeHeadCount = count) }
    }

    // 필터 해제 (검색바 X 버튼 클릭 시)
    private fun clearHeadCountFilter() {
        _uiState.update { it.copy(activeHeadCount = null) }
    }

    // 지도 데이터 가져오기 (필터 상태에 따라 분기)
    private fun fetchStoresInCurrentMap(
        lat: Double,
        lng: Double,
        radius: Double,
        userLat: Double? = null,
        userLng: Double? = null
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val count = _uiState.value.activeHeadCount

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
                _uiState.update { it.copy(isLoading = false) }
                e.printStackTrace()
            }.collect { (stores, _) -> // ★ [수정] Pair 분해하여 stores만 사용
                _uiState.update { it.copy(storeList = stores, isLoading = false) }
            }
        }
    }

    private fun onSearchQueryChanged(
        query: String,
        currentLat: Double,
        currentLng: Double,
        userLat: Double?,
        userLng: Double?
    ) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), relatedUniversities = emptyList()) }
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
        _uiState.update { it.copy(isLoading = true) }
        val count = _uiState.value.activeHeadCount
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
            _uiState.update { it.copy(isLoading = false) }
            e.printStackTrace()
        }.collect { (stores, universities) -> // ★ 구조 분해 선언 (Destructuring)

            _uiState.update {
                it.copy(
                    searchResults = stores,
                    relatedUniversities = universities,
                    isLoading = false
                )
            }
        }
    }

    private fun fetchStoresByUniversity(
        uniName: String,
        lat: Double,
        lng: Double,
        radius: Double,
        userLat: Double?,
        userLng: Double?,
        onResultLoaded: (Store?) -> Unit // 첫 번째 결과 반환용 콜백
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

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
                _uiState.update { it.copy(isLoading = false) }
                e.printStackTrace()
                onResultLoaded(null)
            }.collect { (stores, _) ->
                _uiState.update { it.copy(storeList = stores, isLoading = false) }

                // 검색된 첫 번째 가게 정보를 콜백으로 전달 (지도 이동을 위해)
                onResultLoaded(stores.firstOrNull())
            }
        }
    }

    // 검색 화면 나갈 때 초기화
    private fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList(), relatedUniversities = emptyList()) }
    }
}