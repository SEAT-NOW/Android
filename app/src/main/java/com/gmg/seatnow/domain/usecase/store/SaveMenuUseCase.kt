package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.repository.StoreRepository
import javax.inject.Inject

class SaveMenuUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(
        menuId: Long?, 
        categoryId: Long, 
        name: String, 
        price: Int, 
        imageUri: String?,
        isImageChanged: Boolean
    ): Result<Boolean> {
        return repository.saveMenu(menuId, categoryId, name, price, imageUri, isImageChanged)
    }
}