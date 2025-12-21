package com.gmg.seatnow.domain.model

data class StoreSearchResult(
    val placeName: String,
    val addressName: String,
    val latitude: Double,
    val longitude: Double
)