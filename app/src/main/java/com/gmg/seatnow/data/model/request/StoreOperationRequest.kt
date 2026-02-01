package com.gmg.seatnow.data.model.request

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class StoreOperationRequest(
    @SerializedName("regularHolidays") val regularHolidays: List<RegularHolidayRequest>,
    @SerializedName("temporaryHolidays") val temporaryHolidays: List<TemporaryHolidayRequest>,
    @SerializedName("hours") val hours: List<HourRequest>
)

// ID가 없으면 신규 추가로 처리되므로, 수정 시에는 null을 보냅니다.
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class RegularHolidayRequest(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("dayOfWeek") val dayOfWeek: String,
    @SerializedName("weekInfo") val weekInfo: Int
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class TemporaryHolidayRequest(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class HourRequest(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("dayOfWeek") val dayOfWeek: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String
)