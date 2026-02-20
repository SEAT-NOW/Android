package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.model.StoreMenuCategory
import com.gmg.seatnow.domain.repository.StoreRepository
import javax.inject.Inject

class UpdateMenuOrdersUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(categories: List<StoreMenuCategory>): Result<Boolean> {
        return repository.updateMenuOrders(categories)
    }
}