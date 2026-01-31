package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.repository.StoreRepository
import javax.inject.Inject

class GetStoreImagesUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(): Result<List<String>> {
        return repository.getStoreImages()
    }
}