package com.gmg.seatnow.data.repository

import android.location.Location
import com.gmg.seatnow.data.api.UserApiService
import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.domain.repository.MapRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService
) : MapRepository {

    // ★ [핵심 1] 홈 화면에서 받아온 데이터를 저장하는 캐시 (DB 역할 대행)
    // Key: StoreId, Value: Store(홈 화면 데이터)
    private val storeCache = mutableMapOf<Long, Store>()

    // ★ [핵심 2] 내가 킵한 가게의 ID 목록
    private val keptStoreIds = mutableSetOf<Long>()

    // 1. [홈 화면] API 호출 및 데이터 캐싱
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
                lat = centerLat,
                lng = centerLng,
                headCount = minPerson,
                radius = radius
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data ?: emptyList()

                val stores = data.map { dto ->
                    val calculatedDistance = calculateDistance(userLat, userLng, dto.latitude, dto.longitude)

                    // Domain Model 생성
                    val store = Store(
                        id = dto.storeId,
                        name = dto.storeName,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        status = mapStatus(dto.statusTag),
                        neighborhood = dto.neighborhood ?: "정보 없음",
                        images = dto.images ?: emptyList(),
                        distance = calculatedDistance,
                        operationStatus = dto.operationStatus ?: "영업 정보 없음",
                        storePhone = dto.storePhone
                    )

                    // ★ [중요] 받아온 데이터를 캐시에 저장 (나중에 Detail/Keep에서 쓰기 위해)
                    storeCache[store.id] = store

                    store
                }
                emit(stores)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList())
        }
    }

    // 2. [상세 화면] 캐시된 실제 데이터를 기반으로 상세 정보 생성
    override suspend fun getStoreDetail(storeId: Long): StoreDetail {
        delay(100) // UI 반응성 확인용 딜레이

        // (1) 캐시에서 홈 화면 데이터를 찾음 (실제 DB 데이터)
        val cachedStore = storeCache[storeId]

        // (2) 캐시된 데이터가 있으면 그걸 쓰고, 없으면(혹시 모를 예외) 더미 생성
        val detail = if (cachedStore != null) {
            StoreDetail(
                id = cachedStore.id,
                name = cachedStore.name, // ★ 실제 가게 이름 사용!
                images = cachedStore.images, // ★ 실제 이미지 사용!
                operationStatus = cachedStore.operationStatus,
                storePhone = cachedStore.storePhone ?: "010-0000-0000",
                availableSeatCount = cachedStore.availableSeatCount,
                totalSeatCount = cachedStore.totalSeatCount,
                status = cachedStore.status,
                universityInfo = cachedStore.neighborhood, // 대학 정보 대신 동네 정보라도 우선 매핑
                address = cachedStore.neighborhood, // 상세 주소가 없으니 동네로 대체
                // ▼ 아래 정보는 홈 API에 없으므로 여기만 Mocking (추후 상세 API 생기면 교체)
                openHours = "17:00 ~ 03:00 (정보 없음)",
                closedDays = "연중무휴"
            )
        } else {
            // 캐시에 없는 경우 (거의 없겠지만 방어 코드)
            StoreDetail(
                id = storeId,
                name = "알 수 없는 가게 (ID: $storeId)",
                images = emptyList(),
                operationStatus = "-",
                storePhone = "-",
                availableSeatCount = 0,
                totalSeatCount = 0,
                status = StoreStatus.NORMAL,
                universityInfo = "-",
                address = "-",
                openHours = "-",
                closedDays = "-"
            )
        }

        // (3) 킵 상태 동기화
        return detail.copy(isKept = keptStoreIds.contains(storeId))
    }

    // 3. [킵 화면] 킵한 ID들에 해당하는 데이터를 캐시에서 꺼내 반환
    override suspend fun getKeepStoreList(): Result<List<StoreDetail>> {
        delay(100)

        // keptStoreIds를 순회하며 캐시된 정보를 바탕으로 리스트 생성
        val myKeeps = keptStoreIds.mapNotNull { id ->
            val cachedStore = storeCache[id]

            if (cachedStore != null) {
                StoreDetail(
                    id = cachedStore.id,
                    name = cachedStore.name, // ★ 실제 이름
                    images = cachedStore.images,
                    operationStatus = cachedStore.operationStatus,
                    storePhone = cachedStore.storePhone ?: "",
                    availableSeatCount = cachedStore.availableSeatCount,
                    totalSeatCount = cachedStore.totalSeatCount,
                    status = cachedStore.status,
                    universityInfo = cachedStore.neighborhood,
                    address = cachedStore.neighborhood,
                    openHours = "17:00 ~ 03:00", // Mock
                    closedDays = "연중무휴", // Mock
                    isKept = true // 킵 목록이므로 true
                )
            } else {
                // 캐시가 비워졌는데 ID만 남은 경우 (앱 재실행 등 이슈) -> 제외하거나 처리 필요
                // 여기선 일단 제외 (mapNotNull)
                null
            }
        }

        return Result.success(myKeeps)
    }

    // 4. [킵 토글]
    override suspend fun toggleStoreKeep(storeId: Long, isKept: Boolean): Result<Unit> {
        delay(50)
        if (isKept) {
            keptStoreIds.add(storeId)
        } else {
            keptStoreIds.remove(storeId)
        }
        return Result.success(Unit)
    }

    // [Helper] 거리 계산
    private fun calculateDistance(userLat: Double?, userLng: Double?, storeLat: Double, storeLng: Double): String {
        if (userLat == null || userLng == null) return "0.0km"
        val results = FloatArray(1)
        Location.distanceBetween(userLat, userLng, storeLat, storeLng, results)
        val dist = results[0]
        return if (dist >= 1000) String.format("%.1fkm", dist / 1000) else "${dist.toInt()}m"
    }

    // [Helper] 상태 매핑
    private fun mapStatus(tag: String?): StoreStatus {
        if (tag.isNullOrBlank()) return StoreStatus.NORMAL
        return when (tag.uppercase().trim()) {
            "FREE", "SPACIOUS", "여유" -> StoreStatus.SPARE
            "NORMAL", "보통" -> StoreStatus.NORMAL
            "CROWDED", "혼잡" -> StoreStatus.HARD
            "FULL", "만석" -> StoreStatus.FULL
            else -> StoreStatus.NORMAL
        }
    }
}