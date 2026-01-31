package com.gmg.seatnow.data.model.response

import com.google.gson.annotations.SerializedName

data class StoreOperationResponse(
    @SerializedName("operationStatus") val operationStatus: String?, // "OPEN", "CLOSED", "BREAK_TIME"
    @SerializedName("regularHolidays") val regularHolidays: List<RegularHolidayDTO>,
    @SerializedName("temporaryHolidays") val temporaryHolidays: List<TemporaryHolidayDTO>,
    @SerializedName("openingHours") val openingHours: List<OpeningHourDTO>
)

data class RegularHolidayDTO(
    val id: Long,
    val dayOfWeek: String,
    val weekInfo: Int
)

data class TemporaryHolidayDTO(
    val id: Long,
    val startDate: String,
    val endDate: String
)

data class OpeningHourDTO(
    val id: Long,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String
)