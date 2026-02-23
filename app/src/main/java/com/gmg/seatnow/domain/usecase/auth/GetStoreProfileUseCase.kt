package com.gmg.seatnow.domain.usecase.auth

import com.gmg.seatnow.data.model.response.StoreProfileResponseDTO
import com.gmg.seatnow.domain.repository.AuthRepository
import com.gmg.seatnow.domain.repository.StoreRepository
import javax.inject.Inject

class GetStoreProfileUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(): Result<StoreProfileResponseDTO> {
        return repository.getStoreProfile()
    }
}