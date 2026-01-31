package com.gmg.seatnow.domain.repository

import com.gmg.seatnow.domain.model.OpeningHour
import com.gmg.seatnow.domain.model.RegularHoliday
import com.gmg.seatnow.domain.model.StoreMenuCategory
import com.gmg.seatnow.domain.model.StoreOperationInfo
import com.gmg.seatnow.domain.model.TemporaryHoliday

interface StoreRepository {
    suspend fun getStoreMenus(forceRefresh: Boolean = false): Result<List<StoreMenuCategory>>
    suspend fun getStoreOperations(): Result<StoreOperationInfo>
    suspend fun getStoreImages(): Result<List<String>>
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
}