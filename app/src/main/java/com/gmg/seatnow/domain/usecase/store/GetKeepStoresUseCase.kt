package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.domain.repository.MapRepository
import javax.inject.Inject

class GetKeepStoresUseCase @Inject constructor(
    private val repository: MapRepository
) {
    suspend operator fun invoke(): Result<List<StoreDetail>> {
        return repository.getKeepStoreList()
    }
}