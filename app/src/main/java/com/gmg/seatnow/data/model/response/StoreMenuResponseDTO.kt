package com.gmg.seatnow.data.model.response

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class StoreMenuResponseDTO(
    @SerializedName("categories")
    val categories: List<MenuCategoryDTO>?
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class MenuCategoryDTO(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String,
    @SerializedName("menus")
    val menus: List<MenuItemDTO>?
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
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