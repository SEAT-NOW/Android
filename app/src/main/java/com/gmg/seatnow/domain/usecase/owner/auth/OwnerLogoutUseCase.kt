package com.gmg.seatnow.domain.usecase.owner.auth

import com.gmg.seatnow.domain.repository.AuthRepository
import javax.inject.Inject

class OwnerLogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.ownerLogout()
}

