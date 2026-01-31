package com.gmg.seatnow.data.model.request

import com.google.gson.annotations.SerializedName

data class StoreOperationRequest(
    @SerializedName("regularHolidays") val regularHolidays: List<RegularHolidayRequest>,
    @SerializedName("temporaryHolidays") val temporaryHolidays: List<TemporaryHolidayRequest>,
    @SerializedName("hours") val hours: List<HourRequest>
)

// ID가 없으면 신규 추가로 처리되므로, 수정 시에는 null을 보냅니다.
data class RegularHolidayRequest(
    val id: Long? = null,
    val dayOfWeek: String,
    val weekInfo: Int
)

data class TemporaryHolidayRequest(
    val id: Long? = null,
    val startDate: String,
    val endDate: String
)

data class HourRequest(
    val id: Long? = null,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String
)