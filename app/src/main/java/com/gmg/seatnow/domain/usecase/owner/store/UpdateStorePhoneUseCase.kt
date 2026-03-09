package com.gmg.seatnow.domain.usecase.owner.store

import com.gmg.seatnow.domain.repository.AuthRepository
import com.gmg.seatnow.domain.repository.StoreRepository
import javax.inject.Inject

class UpdateStorePhoneUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(phone: String): Result<Unit> {
        return repository.updateStorePhone(phone)
    }
}