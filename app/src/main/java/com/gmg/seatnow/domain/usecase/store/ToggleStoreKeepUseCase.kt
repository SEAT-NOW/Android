package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.repository.MapRepository
import javax.inject.Inject

class ToggleStoreKeepUseCase @Inject constructor(
    private val repository: MapRepository
) {
    suspend operator fun invoke(storeId: Long, isKept: Boolean): Result<Unit> {
        return repository.toggleStoreKeep(storeId, isKept)
    }
}