package com.gmg.seatnow.domain.repository

import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.OpeningHour
import com.gmg.seatnow.domain.model.RegularHoliday
import com.gmg.seatnow.domain.model.StoreOperationInfo
import com.gmg.seatnow.domain.model.TemporaryHoliday

interface StoreRepository {
    suspend fun getStoreMenus(forceRefresh: Boolean = false): Result<List<MenuCategoryUiModel>>
    suspend fun getStoreOperations(): Result<StoreOperationInfo>
    suspend fun getStoreImages(): Result<List<String>>
    suspend fun updateStoreOperation(
        regularHolidays: List<RegularHoliday>,
        temporaryHolidays: List<TemporaryHoliday>,
        openingHours: List<OpeningHour>
    ): Result<Unit>
}