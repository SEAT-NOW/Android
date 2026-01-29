package com.gmg.seatnow.domain.usecase.auth

import com.gmg.seatnow.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateStorePhoneUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(phone: String): Result<Unit> {
        return repository.updateStorePhone(phone)
    }
}