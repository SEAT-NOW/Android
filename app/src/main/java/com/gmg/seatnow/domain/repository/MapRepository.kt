package com.gmg.seatnow.domain.repository

import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.model.StoreDetail
import kotlinx.coroutines.flow.Flow

interface MapRepository {
    fun getStores(
        keyword: String? = null,
        minPerson: Int,
        centerLat: Double,
        centerLng: Double,
        radius: Double,
        userLat: Double?,
        userLng: Double?)
    : Flow<List<Store>>

    suspend fun getStoreDetail(storeId: Long): Result<Pair<StoreDetail, List<MenuCategoryUiModel>>>
    suspend fun toggleStoreKeep(storeId: Long, isKept: Boolean): Result<Unit>
    suspend fun getKeepStoreList(): Result<List<StoreDetail>>
}