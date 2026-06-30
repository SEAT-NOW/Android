package com.gmg.seatnow.domain.usecase.common.auth

import com.gmg.seatnow.domain.repository.AuthRepository
import javax.inject.Inject

class FindEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phoneNumber: String): Result<String> {
        return authRepository.findEmail(phoneNumber)
    }
}
