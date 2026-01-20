package com.gmg.seatnow.domain.usecase.user

import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStoresByHeadCountUseCase @Inject constructor(
    private val repository: MapRepository
) {
    operator fun invoke(headCount: Int, lat: Double, lng: Double, radius: Double, userLat: Double?, userLng: Double?): Flow<List<Store>> {
        return repository.getStores(headCount, lat, lng, radius, userLat, userLng)
    }
}