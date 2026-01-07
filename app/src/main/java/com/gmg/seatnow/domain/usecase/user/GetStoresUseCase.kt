package com.gmg.seatnow.domain.usecase.user

import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStoresUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    // invoke 연산자를 사용하여 함수처럼 호출 가능하게 만듭니다.
    operator fun invoke(lat: Double, lng: Double): Flow<List<Store>> {
        return repository.getStoresAround(lat, lng)
    }
}