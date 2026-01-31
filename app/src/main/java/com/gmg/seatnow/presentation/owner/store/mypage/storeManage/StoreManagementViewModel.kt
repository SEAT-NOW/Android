package com.gmg.seatnow.presentation.owner.store.mypage.storeManage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.MenuItemUiModel
import com.gmg.seatnow.domain.model.OpeningHour
import com.gmg.seatnow.domain.model.RegularHoliday
import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.domain.usecase.auth.GetOwnerAccountUseCase
import com.gmg.seatnow.domain.usecase.auth.GetStoreProfileUseCase
import com.gmg.seatnow.domain.usecase.store.GetSeatStatusUseCase
import com.gmg.seatnow.domain.usecase.store.GetStoreImagesUseCase
import com.gmg.seatnow.domain.usecase.store.GetStoreMenusUseCase
import com.gmg.seatnow.domain.usecase.store.GetStoreOperationInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreManagementViewModel @Inject constructor(
    private val getOwnerAccountUseCase: GetOwnerAccountUseCase,
    private val getStoreProfileUseCase: GetStoreProfileUseCase,
    private val getSeatStatusUseCase: GetSeatStatusUseCase,
    private val getStoreMenusUseCase: GetStoreMenusUseCase,
    private val getStoreOperationInfoUseCase: GetStoreOperationInfoUseCase,
    private val getStoreImagesUseCase: GetStoreImagesUseCase
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

    fun loadStoreData() {
        viewModelScope.launch {
            _isLoading.update { true }

            val ownerAccountDeferred = async { getOwnerAccountUseCase() }
            val storeProfileDeferred = async { getStoreProfileUseCase() }
            val seatStatusDeferred = async { getSeatStatusUseCase(forceRefresh = false) }
            val storeMenusDeferred = async { getStoreMenusUseCase(forceRefresh = false) }
            val operationInfoDeferred = async { getStoreOperationInfoUseCase() }
            val storeImagesDeferred = async { getStoreImagesUseCase() }

            val ownerResult = ownerAccountDeferred.await()
            val storeResult = storeProfileDeferred.await()
            val seatResult = seatStatusDeferred.await()
            val menuResult = storeMenusDeferred.await()
            val operationResult = operationInfoDeferred.await()
            val imagesResult = storeImagesDeferred.await()

            val ownerData = ownerResult.getOrNull()
            val storeData = storeResult.getOrNull()
            val seatData = seatResult.getOrNull()
            val menuData = menuResult.getOrNull()
            val operationData = operationResult.getOrNull()
            val imagesData = imagesResult.getOrNull() ?: emptyList()

            if (menuResult.isSuccess) {
                val domainCategories = menuResult.getOrNull() ?: emptyList()
                val uiMenus = domainCategories.map { category ->
                    MenuCategoryUiModel(
                        categoryName = category.name,
                        menuItems = category.items.map { item ->
                            MenuItemUiModel(
                                id = item.id,
                                name = item.name,
                                // "22,000" 문자열에서 쉼표 제거 후 Int 변환
                                price = item.price.replace(",", "").toIntOrNull() ?: 0,
                                imageUrl = item.imageUrl ?: "",
                                isRecommended = false,
                                isLiked = false
                            )
                        }
                    )
                }
                _menuListState.update { uiMenus }
            }

            if (storeData != null) {
                // 좌석 계산 로직 (기존 유지)
                var totalSeats = 0
                var availableSeats = 0
                var calculatedStatus = StoreStatus.NORMAL

                if (seatData != null) {
                    totalSeats = seatData.allTables.sumOf { it.capacityPerTable * it.maxTableCount }
                    val usedSeats = seatData.allTables.sumOf { it.capacityPerTable * it.currentCount }
                    availableSeats = (totalSeats - usedSeats).coerceAtLeast(0)
                    calculatedStatus = calculateStoreStatus(totalSeats, usedSeats)
                }

                val finalPhoneNumber = if (!storeData.storePhone.isNullOrBlank()) {
                    storeData.storePhone
                } else {
                    ownerData?.phoneNumber ?: ""
                }

                // ★ [수정] 운영 상태 매핑 (API 값 사용)
                val currentOperationStatus = if (operationData != null) {
                    when (operationData.operationStatus) {
                        "OPEN" -> "영업 중"
                        "CLOSED" -> "영업 종료"
                        "BREAK_TIME" -> "브레이크 타임" // 또는 "휴게 시간"
                        else -> "영업 정보 없음"
                    }
                } else {
                    "영업 정보 없음" // 데이터 로드 실패 시
                }

                // 운영 정보 포맷팅 (기존 유지)
                val formattedOpenHours = if (operationData != null) {
                    formatOpenHours(operationData.openingHours)
                } else {
                    "00:00 ~ 00:00"
                }

                val formattedClosedDays = if (operationData != null) {
                    formatClosedDays(operationData.regularHolidays)
                } else {
                    "연중무휴"
                }

                val mappedDetail = StoreDetail(
                    id = 0L,
                    name = storeData.storeName,
                    address = storeData.address,
                    storePhone = finalPhoneNumber,
                    universityInfo = storeData.universityNames?.joinToString(", ") ?: "",
                    availableSeatCount = availableSeats,
                    totalSeatCount = totalSeats,
                    status = calculatedStatus,
                    images = imagesData,
                    operationStatus = currentOperationStatus, // ★ 매핑된 상태값 주입
                    openHours = formattedOpenHours,
                    closedDays = formattedClosedDays,
                    isKept = false
                )

                _storeDetailState.update { mappedDetail }
            } else {
                storeResult.exceptionOrNull()?.printStackTrace()
            }

            _isLoading.update { false }
        }
    }

    // (하단 Helper 함수들은 기존과 동일하므로 생략하거나 그대로 사용)
    private fun calculateStoreStatus(total: Int, used: Int): StoreStatus {
        if (total == 0) return StoreStatus.NORMAL
        val occupancyRate = (used.toDouble() / total.toDouble()) * 100
        return when {
            occupancyRate >= 100f -> StoreStatus.FULL
            occupancyRate >= 67f -> StoreStatus.HARD
            occupancyRate >= 34f -> StoreStatus.NORMAL
            else -> StoreStatus.SPARE
        }
    }

    private fun formatOpenHours(openingHours: List<OpeningHour>): String {
        if (openingHours.isEmpty()) return "정보 없음"
        val grouped = openingHours.groupBy { "${it.startTime}~${it.endTime}" }
        return grouped.map { (timeRange, hoursList) ->
            val daysStr = hoursList
                .sortedBy { mapDayStringToInt(it.dayOfWeek) }
                .joinToString(", ") { mapDayStringToKorean(it.dayOfWeek) }
            val (start, end) = timeRange.split("~")
            val cleanStart = start.substringBeforeLast(":")
            val cleanEnd = end.substringBeforeLast(":")
            "$daysStr $cleanStart ~ $cleanEnd"
        }.joinToString("\n")
    }

    private fun formatClosedDays(holidays: List<RegularHoliday>): String {
        if (holidays.isEmpty()) return "연중무휴"
        val resultStrings = mutableListOf<String>()

        val weeklyHolidays = holidays.filter { it.weekInfo == 0 }
        if (weeklyHolidays.isNotEmpty()) {
            val days = weeklyHolidays
                .sortedBy { mapDayStringToInt(it.dayOfWeek) }
                .joinToString(", ") { mapDayStringToKorean(it.dayOfWeek) }
            resultStrings.add("매주 ${days}요일")
        }

        val monthlyHolidays = holidays.filter { it.weekInfo != 0 }
        val monthlyGroups = monthlyHolidays.groupBy { it.dayOfWeek }
        monthlyGroups.forEach { (dayOfWeek, list) ->
            val dayStr = mapDayStringToKorean(dayOfWeek)
            val weeksStr = list.map { it.weekInfo }
                .sorted()
                .joinToString(", ") { if (it == 10) "마지막" else "$it" }
            val prefix = if (weeksStr.contains("마지막")) "" else "주"
            resultStrings.add("$weeksStr$prefix ${dayStr}요일")
        }
        return resultStrings.joinToString(" / ") + " 휴무"
    }

    private fun mapDayStringToKorean(day: String): String {
        return when (day.uppercase()) {
            "MONDAY" -> "월"
            "TUESDAY" -> "화"
            "WEDNESDAY" -> "수"
            "THURSDAY" -> "목"
            "FRIDAY" -> "금"
            "SATURDAY" -> "토"
            "SUNDAY" -> "일"
            else -> ""
        }
    }

    private fun mapDayStringToInt(day: String): Int {
        return when (day.uppercase()) {
            "SUNDAY" -> 0
            "MONDAY" -> 1
            "TUESDAY" -> 2
            "WEDNESDAY" -> 3
            "THURSDAY" -> 4
            "FRIDAY" -> 5
            "SATURDAY" -> 6
            else -> 7
        }
    }
}