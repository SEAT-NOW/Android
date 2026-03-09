package com.gmg.seatnow.domain.usecase.owner.auth

import com.gmg.seatnow.data.model.response.OwnerAccountResponseDTO
import com.gmg.seatnow.domain.repository.AuthRepository
import com.gmg.seatnow.domain.repository.StoreRepository
import javax.inject.Inject

class GetOwnerAccountUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(): Result<OwnerAccountResponseDTO> {
        return repository.getOwnerAccount()
    }
}