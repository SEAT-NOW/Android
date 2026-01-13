package com.gmg.seatnow.domain.usecase.auth

import com.gmg.seatnow.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyBusinessNumberUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(businessNumber: String) = repository.verifyBusinessNumber(businessNumber)
}