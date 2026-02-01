package com.gmg.seatnow.data.model.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class StoreDetailResponse(
    @SerializedName("storeId") val storeId: Long,
    @SerializedName("storeName") val storeName: String,
    @SerializedName("address") val address: String,
    @SerializedName("neighborhood") val neighborhood: String,
    @SerializedName("universityNames") val universityNames: List<String>?,
    @SerializedName("storePhone") val storePhone: String?,
    @SerializedName("totalSeatCount") val totalSeatCount: Int,
    @SerializedName("usedSeatCount") val usedSeatCount: Int,
    @SerializedName("statusTag") val statusTag: String,
    @SerializedName("operationStatus") val operationStatus: String,
    @SerializedName("openingHours") val openingHours: List<OpeningHourDto>,
    @SerializedName("regularHolidays") val regularHolidays: List<RegularHolidayDto>,
    @SerializedName("temporaryHolidays") val temporaryHolidays: List<TemporaryHolidayDto>,
    @SerializedName("images") val images: List<ImageDto>,

    // SeatMenuCategory 사용
    @SerializedName("menuCategories") val menuCategories: List<SeatMenuCategory>,

    @SerializedName("kept") val kept: Boolean
)

@Keep
data class SeatMenuCategory(
    val id: Long,
    val name: String,
    val menus: List<SeatMenuDto>
)

@Keep
data class SeatMenuDto(
    val id: Long,
    val name: String,
    val price: Int,
    val imageUrl: String?
)

@Keep
data class OpeningHourDto(val id: Long, val dayOfWeek: String, val startTime: String, val endTime: String)

@Keep
data class RegularHolidayDto(val id: Long, val dayOfWeek: String, val weekInfo: Int)

@Keep
data class TemporaryHolidayDto(val id: Long, val startDate: String, val endDate: String)

@Keep
data class ImageDto(val id: Long, val url: String, val isMain: Boolean)