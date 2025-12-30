package com.gmg.seatnow.domain.usecase

import com.gmg.seatnow.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyAuthCodeUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(target: String, code: String): Result<Unit> {
        return repository.verifyAuthCode(target, code)
    }
}