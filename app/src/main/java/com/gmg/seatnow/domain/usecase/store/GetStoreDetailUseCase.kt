package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.domain.repository.MapRepository
import javax.inject.Inject

class GetStoreDetailUseCase @Inject constructor(
    private val repository: MapRepository
) {
    // 반환 타입을 Repository와 동일하게 Result<Pair<StoreDetail, List<MenuCategoryUiModel>>>로 변경
    suspend operator fun invoke(storeId: Long): Result<Pair<StoreDetail, List<MenuCategoryUiModel>>> {
        return repository.getStoreDetail(storeId)
    }
}