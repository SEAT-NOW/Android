package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.repository.StoreRepository
import javax.inject.Inject

class DeleteMenuUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(menuId: Long): Result<Boolean> {
        return repository.deleteMenu(menuId)
    }
}