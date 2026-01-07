package com.gmg.seatnow.domain.repository

import com.gmg.seatnow.domain.model.Store
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    fun getStoresAround(centerLat: Double, centerLng: Double): Flow<List<Store>>
}