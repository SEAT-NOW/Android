package com.gmg.seatnow.domain.usecase.user

import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStoresUseCase @Inject constructor(
    private val repository: MapRepository
) {
    operator fun invoke(
        keyword: String? = null, // ★ [추가]
        lat: Double,
        lng: Double,
        radius: Double,
        userLat: Double? = null,
        userLng: Double? = null
    ): Flow<List<Store>> {
        return repository.getStores(
            keyword = keyword,
            minPerson = 0,
            centerLat = lat,
            centerLng = lng,
            radius = radius,
            userLat = userLat,
            userLng = userLng
        )
    }
}