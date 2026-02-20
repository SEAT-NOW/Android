package com.gmg.seatnow.domain.repository

import com.gmg.seatnow.domain.model.OpeningHour
import com.gmg.seatnow.domain.model.RegularHoliday
import com.gmg.seatnow.domain.model.StoreMenuCategory
import com.gmg.seatnow.domain.model.StoreOperationInfo
import com.gmg.seatnow.domain.model.TemporaryHoliday
import com.gmg.seatnow.domain.model.StoreImage
import com.gmg.seatnow.presentation.owner.store.storeManage.storeManageEdit.StoreImageUiModel // UI 모델 import 필요

interface StoreRepository {
    suspend fun getStoreMenus(forceRefresh: Boolean = false): Result<List<StoreMenuCategory>>
    suspend fun getStoreOperations(): Result<StoreOperationInfo>
    suspend fun getStoreImages(): Result<List<StoreImage>>
    suspend fun updateStoreImages(currentImages: List<StoreImageUiModel>): Result<Boolean>
    suspend fun updateStoreOperation(
        regularHolidays: List<RegularHoliday>,
        temporaryHolidays: List<TemporaryHoliday>,
        openingHours: List<OpeningHour>
    ): Result<Unit>
    suspend fun updateMenuCategories(categories: List<StoreMenuCategory>): Result<Boolean>
    suspend fun saveMenu(
        menuId: Long?,
        categoryId: Long,
        name: String,
        price: Int,
        imageUri: String?,
        isImageChanged: Boolean // 이미지가 변경되었는지 (갤러리에서 선택했는지)
    ): Result<Boolean>

    suspend fun updateMenuOrders(categories: List<StoreMenuCategory>): Result<Boolean>
    suspend fun deleteMenu(menuId: Long): Result<Boolean>
}