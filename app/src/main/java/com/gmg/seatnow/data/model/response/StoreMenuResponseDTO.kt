package com.gmg.seatnow.data.model.response

import com.google.gson.annotations.SerializedName

data class StoreMenuResponseDTO(
    @SerializedName("categories")
    val categories: List<MenuCategoryDTO>?
)

data class MenuCategoryDTO(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("menus")
    val menus: List<MenuItemDTO>?
)

data class MenuItemDTO(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("price")
    val price: Int,
    @SerializedName("imageUrl")
    val imageUrl: String?
)