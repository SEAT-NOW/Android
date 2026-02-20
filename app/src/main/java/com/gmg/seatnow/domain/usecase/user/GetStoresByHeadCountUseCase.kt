package com.gmg.seatnow.domain.usecase.user

import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStoresByHeadCountUseCase @Inject constructor(
    private val repository: MapRepository
) {
    operator fun invoke(
        headCount: Int,
        keyword: String? = null,
        lat: Double,
        lng: Double,
        radius: Double,
        userLat: Double?,
        userLng: Double?
    ): Flow<Pair<List<Store>, List<String>>> { // ★ 반환 타입 변경
        return repository.getStores(
            keyword = keyword,
            universityName = null,
            minPerson = headCount,
            centerLat = lat,
            centerLng = lng,
            radius = radius,
            userLat = userLat,
            userLng = userLng
        )
    }
}