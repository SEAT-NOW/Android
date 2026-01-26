package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.domain.repository.MapRepository
import javax.inject.Inject

class GetStoreDetailUseCase @Inject constructor(
    private val repository: MapRepository
) {
    suspend operator fun invoke(storeId: Long): StoreDetail {
        return repository.getStoreDetail(storeId)
    }
}