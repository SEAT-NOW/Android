package com.gmg.seatnow.presentation.user.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.usecase.user.GetStoresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserHomeViewModel @Inject constructor(
    private val getStoresUseCase: GetStoresUseCase
) : ViewModel() {

    private val _storeList = MutableStateFlow<List<Store>>(emptyList())
    val storeList: StateFlow<List<Store>> = _storeList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // "현 지도에서 검색" 버튼 클릭 시 호출
    fun fetchStoresInCurrentMap(lat: Double, lng: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            getStoresUseCase(lat, lng)
                .catch { e ->
                    // 에러 처리 (로그 등)
                    _isLoading.value = false
                    e.printStackTrace()
                }
                .collect { stores ->
                    _storeList.value = stores
                    _isLoading.value = false
                }
        }
    }
}