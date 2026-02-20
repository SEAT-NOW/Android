package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.repository.StoreRepository
import com.gmg.seatnow.presentation.owner.store.storeManage.storeManageEdit.StoreImageUiModel
import javax.inject.Inject

class UpdateStoreImagesUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(currentImages: List<StoreImageUiModel>): Result<Boolean> {
        return repository.updateStoreImages(currentImages)
    }
}