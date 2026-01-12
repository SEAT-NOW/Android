package com.gmg.seatnow.domain.usecase.user

import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.repository.MapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStoresByHeadCountUseCase @Inject constructor(
    private val repository: MapRepository
) {
    operator fun invoke(headCount: Int, lat: Double, lng: Double): Flow<List<Store>> {
        // ★ 수정됨: 별도 API 함수 대신 통합된 getStores를 사용하고 headCount를 넘깁니다.
        return repository.getStores(minPerson = headCount, centerLat = lat, centerLng = lng)
    }
}