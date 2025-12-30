package com.gmg.seatnow.domain.usecase

import com.gmg.seatnow.domain.repository.AuthRepository
import javax.inject.Inject

class RequestAuthCodeUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(target: String): Result<Unit> {
        return repository.requestAuthCode(target)
    }
}