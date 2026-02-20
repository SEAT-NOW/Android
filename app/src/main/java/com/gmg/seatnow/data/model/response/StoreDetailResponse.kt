package com.gmg.seatnow.data.model.response

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class StoreDetailResponse(
    @SerializedName("storeId") val storeId: Long,
    @SerializedName("storeName") val storeName: String,
    @SerializedName("address") val address: String,
    @SerializedName("neighborhood") val neighborhood: String,
    @SerializedName("universityNames") val universityNames: List<String>?,
    @SerializedName("storePhone") val storePhone: String?,
    @SerializedName("totalSeatCount") val totalSeatCount: Int,
    @SerializedName("usedSeatCount") val usedSeatCount: Int,
    @SerializedName("statusTagName") val statusTagName: String,
    @SerializedName("operationStatus") val operationStatus: String,
    @SerializedName("openingHours") val openingHours: List<OpeningHourItem>,
    @SerializedName("regularHolidays") val regularHolidays: List<RegularHolidayItem>,
    @SerializedName("temporaryHolidays") val temporaryHolidays: List<TemporaryHolidayItem>,
    @SerializedName("images") val images: List<ImageItem>,

    // SeatMenuCategory 사용
    @SerializedName("menuCategories") val menuCategories: List<SeatMenuCategory>,

    @SerializedName("kept") val kept: Boolean
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class SeatMenuCategory(
    val id: Long,
    val name: String,
    val menus: List<SeatMenuItem>
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class SeatMenuItem(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("price") val price: Int,
    @SerialName("imageUrl") val imageUrl: String?,
    @SerializedName("isBest") val isBest: Boolean = false,
    @SerializedName("isLiked") val isLiked: Boolean = false
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class OpeningHourItem(
    @SerialName("id") val id: Long,
    @SerialName("dayOfWeek") val dayOfWeek: String,
    @SerialName("startTime") val startTime: String,
    @SerialName("endTime") val endTime: String
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class RegularHolidayItem(
    @SerialName("id") val id: Long,
    @SerialName("dayOfWeek") val dayOfWeek: String,
    @SerialName("weekInfo") val weekInfo: Int
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class TemporaryHolidayItem(
    @SerialName("id") val id: Long,
    @SerialName("startDate") val startDate: String,
    @SerialName("endDate") val endDate: String
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class ImageItem(
    @SerialName("id") val id: Long,
    @SerialName("url") val url: String,
    @SerialName("isMain") val isMain: Boolean
)