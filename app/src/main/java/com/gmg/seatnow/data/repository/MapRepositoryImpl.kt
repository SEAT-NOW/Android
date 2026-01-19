package com.gmg.seatnow.data.repository

import com.gmg.seatnow.data.api.UserApiService
import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService
) : MapRepository {

    override fun getStores(minPerson: Int, centerLat: Double, centerLng: Double, radius: Double): Flow<List<Store>> = flow {
        try {
            // API 호출 (minPerson이 0이면 전체, N이면 필터링)
            val response = userApiService.getStoresOnMap(
                lat = centerLat,
                lng = centerLng,
                headCount = minPerson,
                radius = radius
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data ?: emptyList()

                // DTO -> Domain Model 변환 (필요한 정보만 매핑)
                val stores = data.map { dto ->
                    Store(
                        id = dto.storeId,
                        name = dto.storeName,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        // 서버의 String 상태값("CROWDED") -> 앱 내부 Enum으로 변환
                        status = mapStatus(dto.statusTag)
                    )
                }
                emit(stores)
            } else {
                // 실패 시 빈 리스트 반환 (혹은 에러 처리)
                emit(emptyList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(emptyList()) // 에러 발생 시 안전하게 빈 리스트
        }
    }

    // [Helper] 서버 상태값 -> 앱 내부 Enum 변환
    private fun mapStatus(tag: String?): StoreStatus {
        return when (tag) {
            "SPACIOUS" -> StoreStatus.SPARE  // 여유
            "NORMAL" -> StoreStatus.NORMAL      // 보통
            "CROWDED" -> StoreStatus.HARD    // 혼잡
            "FULL" -> StoreStatus.FULL          // 만석
            else -> StoreStatus.NORMAL          // 기본값
        }
    }
}