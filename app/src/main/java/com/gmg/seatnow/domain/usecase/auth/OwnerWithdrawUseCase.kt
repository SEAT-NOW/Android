package com.gmg.seatnow.domain.usecase.auth

import com.gmg.seatnow.domain.repository.AuthRepository
import javax.inject.Inject

class OwnerWithdrawUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(businessNumber: String, password: String): Result<Unit> {
        return repository.ownerWithdraw(businessNumber, password)
    }
}