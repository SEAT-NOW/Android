package com.gmg.seatnow.domain.repository

import com.gmg.seatnow.domain.model.MenuCategoryUiModel

interface StoreRepository {
    suspend fun getStoreMenus(forceRefresh: Boolean = false): Result<List<MenuCategoryUiModel>>
}