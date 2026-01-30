package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.repository.StoreRepository
import javax.inject.Inject

class GetStoreMenusUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<List<MenuCategoryUiModel>> {
        return repository.getStoreMenus(forceRefresh)
    }
}