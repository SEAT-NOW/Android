package com.gmg.seatnow.data.model.response

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class StoreOperationResponse(
    @SerializedName("operationStatus") val operationStatus: String?, // "OPEN", "CLOSED", "BREAK_TIME"
    @SerializedName("regularHolidays") val regularHolidays: List<RegularHolidayDTO>,
    @SerializedName("temporaryHolidays") val temporaryHolidays: List<TemporaryHolidayDTO>,
    @SerializedName("openingHours") val openingHours: List<OpeningHourDTO>
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class RegularHolidayDTO(
    @SerializedName("id") val id: Long,
    @SerializedName("dayOfWeek") val dayOfWeek: String,
    @SerializedName("weekInfo") val weekInfo: Int
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class TemporaryHolidayDTO(
    @SerializedName("id") val id: Long,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class OpeningHourDTO(
    @SerializedName("id") val id: Long,
    @SerializedName("dayOfWeek") val dayOfWeek: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String
)