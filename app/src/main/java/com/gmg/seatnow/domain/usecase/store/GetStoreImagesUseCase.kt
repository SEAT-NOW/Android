// domain/usecase/store/GetStoreImagesUseCase.kt
package com.gmg.seatnow.domain.usecase.store


import com.gmg.seatnow.domain.model.StoreImage
import com.gmg.seatnow.domain.repository.StoreRepository
import javax.inject.Inject

class GetStoreImagesUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    // ★ [변경] List<String> -> List<StoreImage>
    suspend operator fun invoke(): Result<List<StoreImage>> {
        return repository.getStoreImages()
    }
}