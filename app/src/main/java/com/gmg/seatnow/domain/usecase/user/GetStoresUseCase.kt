package com.gmg.seatnow.domain.usecase.user

import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStoresUseCase @Inject constructor(
    private val repository: MapRepository
) {
    operator fun invoke(lat: Double, lng: Double, radius: Double, userLat: Double?, userLng: Double?): Flow<List<Store>> {
        return repository.getStores(0, lat, lng, radius, userLat, userLng)
    }
}