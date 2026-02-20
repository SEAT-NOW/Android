package com.gmg.seatnow.domain.model

data class StoreMenuCategory(
    val id: Long,
    val name: String,
    val items: List<StoreMenuItemData> = emptyList()
)

data class StoreMenuItemData(
    val id: Long,
    val name: String,
    val price: String, // "22,000" 형태
    val imageUrl: String? = null
)