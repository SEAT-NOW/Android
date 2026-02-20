package com.gmg.seatnow.domain.usecase.user

import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStoresUseCase @Inject constructor(
    private val repository: MapRepository
) {
    operator fun invoke(
        keyword: String? = null,
        universityName: String? = null,
        lat: Double,
        lng: Double,
        radius: Double,
        userLat: Double? = null,
        userLng: Double? = null
    ): Flow<Pair<List<Store>, List<String>>> { // ★ 반환 타입 변경
        return repository.getStores(
            keyword = keyword,
            universityName = universityName,
            minPerson = 0,
            centerLat = lat,
            centerLng = lng,
            radius = radius,
            userLat = userLat,
            userLng = userLng
        )
    }
}