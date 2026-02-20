package com.gmg.seatnow.domain.repository

import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.model.StoreDetail
import kotlinx.coroutines.flow.Flow

interface MapRepository {
    fun getStores(
        keyword: String? = null,
        universityName: String? = null,
        minPerson: Int,
        centerLat: Double,
        centerLng: Double,
        radius: Double,
        userLat: Double?,
        userLng: Double?)
    : Flow<Pair<List<Store>, List<String>>>

    suspend fun getStoreDetail(storeId: Long): Result<Pair<StoreDetail, List<MenuCategoryUiModel>>>
    suspend fun toggleStoreKeep(storeId: Long, isKept: Boolean): Result<Unit>
    suspend fun getKeepStoreList(): Result<List<StoreDetail>>
    suspend fun toggleMenuLike(menuId: Long): Result<Boolean>
}