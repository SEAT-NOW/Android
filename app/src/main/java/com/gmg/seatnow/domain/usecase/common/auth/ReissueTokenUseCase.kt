package com.gmg.seatnow.domain.usecase.common.auth

import com.gmg.seatnow.domain.repository.AuthRepository
import javax.inject.Inject

class ReissueTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.reissueToken()
    }
}