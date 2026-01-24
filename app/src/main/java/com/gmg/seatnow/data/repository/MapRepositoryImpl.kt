package com.gmg.seatnow.data.repository

import com.gmg.seatnow.data.api.UserApiService
import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import android.location.Location
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService
) : MapRepository {

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
                    // 거리 직접 계산 로직
                    val calculatedDistance = calculateDistance(userLat, userLng, dto.latitude, dto.longitude)

                    Store(
                        id = dto.storeId,
                        name = dto.storeName,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        status = mapStatus(dto.statusTag),
                        neighborhood = dto.neighborhood ?: "정보 없음",
                        images = dto.images ?: emptyList(),
                        distance = calculatedDistance,
                        operationStatus = dto.operationStatus ?: "영업 정보 없음",
                        storePhone = dto.storePhone // [추가] 전화번호 매핑
                    )
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

    // [Helper] 거리 계산 함수
    private fun calculateDistance(userLat: Double?, userLng: Double?, storeLat: Double, storeLng: Double): String {
        // GPS 정보가 없으면 "0.0km" 반환 (요청사항)
        if (userLat == null || userLng == null) return "0.0km"

        val results = FloatArray(1)
        Location.distanceBetween(userLat, userLng, storeLat, storeLng, results)
        val distanceInMeters = results[0]

        return if (distanceInMeters >= 1000) {
            String.format("%.1fkm", distanceInMeters / 1000) // 1km 이상
        } else {
            "${distanceInMeters.toInt()}m" // 1km 미만
        }
    }

    // [Helper] 서버 상태값 -> 앱 내부 Enum 변환
    private fun mapStatus(tag: String?): StoreStatus {
        if (tag.isNullOrBlank()) return StoreStatus.NORMAL

        // ★ 로그에 찍힌 "FREE"를 처리하도록 수정
        return when (tag.uppercase().trim()) {
            "FREE", "SPACIOUS", "여유" -> StoreStatus.SPARE   // "FREE"가 오면 '여유'로 매핑
            "NORMAL", "보통" -> StoreStatus.NORMAL
            "CROWDED", "혼잡" -> StoreStatus.HARD
            "FULL", "만석" -> StoreStatus.FULL
            else -> StoreStatus.NORMAL // 그 외 값은 보통으로 처리
        }
    }

}