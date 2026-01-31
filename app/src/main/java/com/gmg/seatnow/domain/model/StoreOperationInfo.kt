package com.gmg.seatnow.domain.model

data class StoreOperationInfo(
    val operationStatus: String, // 도메인에 전달할 상태값
    val regularHolidays: List<RegularHoliday>,
    val temporaryHolidays: List<TemporaryHoliday>,
    val openingHours: List<OpeningHour>
)

data class RegularHoliday(val dayOfWeek: String, val weekInfo: Int)
data class TemporaryHoliday(val startDate: String, val endDate: String)
data class OpeningHour(val dayOfWeek: String, val startTime: String, val endTime: String)