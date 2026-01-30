package com.gmg.seatnow.presentation.owner.store.mypage.storeManage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.domain.usecase.auth.GetOwnerAccountUseCase
import com.gmg.seatnow.domain.usecase.auth.GetStoreProfileUseCase
import com.gmg.seatnow.domain.usecase.store.GetSeatStatusUseCase
import com.gmg.seatnow.domain.usecase.store.GetStoreMenusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class StoreManagementViewModel @Inject constructor(
    private val getOwnerAccountUseCase: GetOwnerAccountUseCase,
    private val getStoreProfileUseCase: GetStoreProfileUseCase,
    private val getSeatStatusUseCase: GetSeatStatusUseCase,
    private val getStoreMenusUseCase: GetStoreMenusUseCase
) : ViewModel() {

    private val _storeDetailState = MutableStateFlow<StoreDetail?>(null)
    val storeDetailState: StateFlow<StoreDetail?> = _storeDetailState.asStateFlow()

    private val _menuListState = MutableStateFlow<List<MenuCategoryUiModel>>(emptyList())
    val menuListState: StateFlow<List<MenuCategoryUiModel>> = _menuListState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadStoreData()
    }

    private fun loadStoreData() {
        viewModelScope.launch {
            _isLoading.update { true }

            // 1. 병렬 호출로 데이터 로딩 최적화
            val ownerAccountDeferred = async { getOwnerAccountUseCase() }
            val storeProfileDeferred = async { getStoreProfileUseCase() }
            val seatStatusDeferred = async { getSeatStatusUseCase(forceRefresh = false) }
            val storeMenusDeferred = async { getStoreMenusUseCase(forceRefresh = false) } // ★ 메뉴 호출

            val ownerResult = ownerAccountDeferred.await()
            val storeResult = storeProfileDeferred.await()
            val seatResult = seatStatusDeferred.await()
            val menuResult = storeMenusDeferred.await()

            val ownerData = ownerResult.getOrNull()
            val storeData = storeResult.getOrNull()
            val seatData = seatResult.getOrNull()
            val menuData = menuResult.getOrNull()

            if (menuData != null) {
                _menuListState.update { menuData }
            }

            if (storeData != null) {
                // 2. 좌석 점유율 및 상태 계산
                var totalSeats = 0
                var availableSeats = 0
                // 기본값은 NORMAL (데이터가 없을 경우 대비)
                var calculatedStatus = StoreStatus.NORMAL

                if (seatData != null) {
                    totalSeats = seatData.allTables.sumOf { it.capacityPerTable * it.maxTableCount }
                    val usedSeats = seatData.allTables.sumOf { it.capacityPerTable * it.currentCount }
                    availableSeats = (totalSeats - usedSeats).coerceAtLeast(0)

                    // ★ [요청하신 로직 적용] 점유율 구간에 따른 Status 결정
                    calculatedStatus = calculateStoreStatus(totalSeats, usedSeats)
                }

                // 3. 연락처 결정
                val finalPhoneNumber = if (!storeData.storePhone.isNullOrBlank()) {
                    storeData.storePhone
                } else {
                    ownerData?.phoneNumber ?: ""
                }

                // 4. UI 모델 매핑
                val mappedDetail = StoreDetail(
                    id = 0L,
                    name = storeData.storeName,
                    address = storeData.address,
                    storePhone = finalPhoneNumber,
                    universityInfo = storeData.universityNames?.joinToString(", ") ?: "",

                    availableSeatCount = availableSeats,
                    totalSeatCount = totalSeats,

                    status = calculatedStatus,

                    images = emptyList(),
                    operationStatus = "영업 중",
                    openHours = "00:00 ~ 00:00",
                    closedDays = "연중무휴",
                    isKept = false
                )

                _storeDetailState.update { mappedDetail }
            } else {
                storeResult.exceptionOrNull()?.printStackTrace()
            }

            _isLoading.update { false }
        }
    }

    // ★ 점유율 기반 상태 계산 함수 (요청 사항 반영)
    private fun calculateStoreStatus(total: Int, used: Int): StoreStatus {
        if (total == 0) return StoreStatus.NORMAL // 분모가 0일 때 예외 처리

        val occupancyRate = (used.toDouble() / total.toDouble()) * 100

        return when {
            occupancyRate >= 100f -> StoreStatus.FULL  // 100% (만석)
            occupancyRate >= 67f -> StoreStatus.HARD   // 67% ~ 99% (혼잡)
            occupancyRate >= 34f -> StoreStatus.NORMAL // 34% ~ 66% (보통)
            else -> StoreStatus.SPARE                   // 0% ~ 33% (한적)
        }
    }
}