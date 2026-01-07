package com.gmg.seatnow.domain.usecase.user

import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStoresUseCase @Inject constructor(
    private val repository: MapRepository
) {
    operator fun invoke(lat: Double, lng: Double): Flow<List<Store>> {
        // 홈에서는 필터링이 없으므로 minPerson에 0을 넣어 호출
        return repository.getStores(minPerson = 0, centerLat = lat, centerLng = lng)
    }
}