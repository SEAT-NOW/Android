package com.gmg.seatnow.data.repository

import android.location.Location
import com.gmg.seatnow.data.api.UserApiService

// ★ [수정] 변경된 DTO 이름으로 정확히 Import 해야 합니다!
import com.gmg.seatnow.data.model.response.OpeningHourItem
import com.gmg.seatnow.data.model.response.RegularHolidayItem

import com.gmg.seatnow.domain.model.*
import com.gmg.seatnow.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService
) : MapRepository {

    private val storeCache = mutableMapOf<Long, Store>()

    // 1. [홈] 지도 위 매장 검색
    override fun getStores(
        keyword: String?,
        minPerson: Int,
        centerLat: Double,
        centerLng: Double,
        radius: Double,
        userLat: Double?,
        userLng: Double?
    ): Flow<List<Store>> = flow {
        try {
            val response = userApiService.getStoresOnMap(
                keyword = keyword,
                headCount = if (minPerson > 0) minPerson else null,
                lat = centerLat,
                lng = centerLng,
                radius = radius
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val dtoList = response.body()?.data ?: emptyList()
                val domainList = dtoList.map { dto ->
                    val distStr = calculateDistance(userLat, userLng, dto.latitude, dto.longitude)
                    val imageList = dto.images ?: emptyList()

                    Store(
                        id = dto.storeId,
                        name = dto.storeName,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        status = mapStatus(dto.statusTag),
                        imageUrl = imageList.firstOrNull(),
                        neighborhood = dto.neighborhood ?: "",
                        images = imageList,
                        distance = distStr,
                        operationStatus = dto.operationStatus ?: "정보 없음",
                        storePhone = dto.storePhone,
                        availableSeatCount = dto.availableSeatCount,
                        totalSeatCount = dto.totalSeatCount
                    )
                }
                domainList.forEach { storeCache[it.id] = it }
                emit(domainList)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    // 2. [상세] 매장 상세 정보 조회
    override suspend fun getStoreDetail(storeId: Long): Result<Pair<StoreDetail, List<MenuCategoryUiModel>>> {
        return try {
            val response = userApiService.getStoreDetail(storeId)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data!!

                // 상세 API의 images는 List<ImageDto> 이므로 변환 필요
                val detailImages = data.images.sortedByDescending { it.isMain }.map { it.url }

                val storeDetail = StoreDetail(
                    id = data.storeId,
                    name = data.storeName,
                    images = detailImages,
                    operationStatus = mapOperationStatus(data.operationStatus),
                    storePhone = data.storePhone ?: "전화번호 없음",
                    availableSeatCount = (data.totalSeatCount - data.usedSeatCount).coerceAtLeast(0),
                    totalSeatCount = data.totalSeatCount,
                    status = mapStatus(data.statusTag),
                    universityInfo = data.universityNames?.joinToString(", ") ?: "주변 대학 정보 없음",
                    address = "${data.address} ${data.neighborhood}",
                    openHours = formatOpeningHours(data.openingHours),
                    closedDays = formatClosedDays(data.regularHolidays),
                    isKept = data.kept
                )

                // ★ [수정 확인] SeatMenuCategory -> MenuCategoryUiModel 매핑
                val menus = data.menuCategories.map { category ->
                    MenuCategoryUiModel(
                        categoryName = category.name,
                        menuItems = category.menus.map { menu ->
                            MenuItemUiModel(
                                id = menu.id,
                                name = menu.name,
                                price = menu.price,
                                imageUrl = menu.imageUrl ?: "",
                                isRecommended = false,
                                isLiked = false
                            )
                        }
                    )
                }

                Result.success(storeDetail to menus)
            } else {
                Result.failure(Exception(response.body()?.message ?: "상세 정보를 불러오지 못했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. 킵 토글
    override suspend fun toggleStoreKeep(storeId: Long, isKept: Boolean): Result<Unit> {
        return try {
            // [수정] scrapStore -> keepStore 호출로 변경
            val response = userApiService.keepStore(storeId)

            if (response.isSuccessful && response.body()?.success == true) {
                // 성공 시 Unit 반환 (ViewModel에서 UI 갱신)
                Result.success(Unit)
            } else {
                // 에러 처리
                when (response.code()) {
                    401 -> Result.failure(Exception("Unauthorized")) // 토큰 만료/없음
                    403 -> Result.failure(Exception("Forbidden"))    // 권한 없음
                    else -> Result.failure(Exception(response.message()))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getKeepStoreList(): Result<List<StoreDetail>> {
        return try {
            val response = userApiService.getKeptStores()

            if (response.isSuccessful && response.body()?.success == true) {
                val dtoList = response.body()?.data ?: emptyList()

                val domainList = dtoList.map { dto ->
                    val available = (dto.totalSeatCount - dto.usedSeatCount).coerceAtLeast(0)

                    // ★ [수정] 이미지가 String 하나로 오므로 리스트로 감싸줌
                    val imageList = if (!dto.images.isNullOrEmpty()) listOf(dto.images) else emptyList()

                    StoreDetail(
                        id = dto.storeId,
                        name = dto.storeName,
                        images = imageList, // 수정된 이미지 처리

                        // ★ [수정] List<String> -> String 변환 (콤마로 연결)
                        universityInfo = dto.universityNames?.joinToString(", ") ?: "",

                        status = mapStatusByName(dto.statusTagName),
                        availableSeatCount = available,
                        totalSeatCount = dto.totalSeatCount,
                        operationStatus = "",
                        storePhone = "",
                        address = "",
                        openHours = "",
                        closedDays = "",
                        isKept = true
                    )
                }
                Result.success(domainList)
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ★ [필수 추가] 한글 상태명("혼잡", "만석")을 Enum으로 변환하는 함수
    private fun mapStatusByName(name: String?): StoreStatus {
        return when (name) {
            "여유" -> StoreStatus.SPARE
            "보통" -> StoreStatus.NORMAL
            "혼잡" -> StoreStatus.HARD
            "만석" -> StoreStatus.FULL
            else -> StoreStatus.NORMAL // 기본값
        }
    }

    // --- Helper Functions ---
    private fun calculateDistance(userLat: Double?, userLng: Double?, storeLat: Double, storeLng: Double): String {
        if (userLat == null || userLng == null) return "0.0km"
        val results = FloatArray(1)
        Location.distanceBetween(userLat, userLng, storeLat, storeLng, results)
        val dist = results[0]
        return if (dist >= 1000) String.format("%.1fkm", dist / 1000) else "${dist.toInt()}m"
    }

    private fun mapStatus(tag: String?): StoreStatus {
        return when (tag) {
            "FREE" -> StoreStatus.SPARE
            "NORMAL" -> StoreStatus.NORMAL
            "BUSY", "CROWDED" -> StoreStatus.HARD
            "FULL" -> StoreStatus.FULL
            else -> StoreStatus.NORMAL
        }
    }

    private fun mapOperationStatus(status: String): String {
        return when (status) {
            "OPEN" -> "영업 중"
            "CLOSED" -> "영업 종료"
            "BREAK_TIME" -> "브레이크 타임"
            else -> "정보 없음"
        }
    }

    private fun formatOpeningHours(hours: List<OpeningHourItem>): String {
        if (hours.isEmpty()) return "영업 시간 정보 없음"
        val grouped = hours.groupBy { "${it.startTime}~${it.endTime}" }
        return grouped.map { (timeRange, list) ->
            val days = list.sortedBy { dayOrder(it.dayOfWeek) }
                .joinToString(", ") { mapDayToKorean(it.dayOfWeek) }
            val (start, end) = timeRange.split("~")
            val s = if (start.length >= 5) start.substring(0, 5) else start
            val e = if (end.length >= 5) end.substring(0, 5) else end
            "$days $s ~ $e"
        }.joinToString("\n")
    }

    private fun formatClosedDays(holidays: List<RegularHolidayItem>): String {
        if (holidays.isEmpty()) return "연중무휴"
        val weekly = holidays.filter { it.weekInfo == 0 }
            .sortedBy { dayOrder(it.dayOfWeek) }
            .joinToString(", ") { "${mapDayToKorean(it.dayOfWeek)}요일" }
        return if (weekly.isNotEmpty()) "매주 $weekly 휴무" else "휴무 없음"
    }

    private fun mapDayToKorean(day: String) = when (day) {
        "MONDAY" -> "월"; "TUESDAY" -> "화"; "WEDNESDAY" -> "수"; "THURSDAY" -> "목"
        "FRIDAY" -> "금"; "SATURDAY" -> "토"; "SUNDAY" -> "일"; else -> ""
    }

    private fun dayOrder(day: String) = when (day) {
        "MONDAY" -> 1; "TUESDAY" -> 2; "WEDNESDAY" -> 3; "THURSDAY" -> 4
        "FRIDAY" -> 5; "SATURDAY" -> 6; "SUNDAY" -> 7; else -> 8
    }
}