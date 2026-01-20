package com.gmg.seatnow.domain.repository

import com.gmg.seatnow.domain.model.Store
import kotlinx.coroutines.flow.Flow

interface MapRepository {
    fun getStores(minPerson: Int, centerLat: Double, centerLng: Double, radius: Double, userLat: Double?, userLng: Double?): Flow<List<Store>>}