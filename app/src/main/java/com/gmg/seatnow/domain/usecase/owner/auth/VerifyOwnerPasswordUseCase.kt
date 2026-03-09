package com.gmg.seatnow.domain.usecase.owner.auth

import com.gmg.seatnow.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyOwnerPasswordUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(password: String): Result<Unit> {
        return repository.verifyOwnerPassword(password)
    }
}