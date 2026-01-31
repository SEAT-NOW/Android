package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.model.StoreOperationInfo
import com.gmg.seatnow.domain.repository.StoreRepository
import javax.inject.Inject

class GetStoreOperationInfoUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(): Result<StoreOperationInfo> {
        return repository.getStoreOperations()
    }
}