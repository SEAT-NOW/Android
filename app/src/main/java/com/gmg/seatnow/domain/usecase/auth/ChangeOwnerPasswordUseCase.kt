package com.gmg.seatnow.domain.usecase.auth

import com.gmg.seatnow.domain.repository.AuthRepository
import javax.inject.Inject

class ChangeOwnerPasswordUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(password: String): Result<Unit> {
        return repository.changeOwnerPassword(password)
    }
}