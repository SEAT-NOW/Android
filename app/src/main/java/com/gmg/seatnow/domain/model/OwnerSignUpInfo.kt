package com.gmg.seatnow.domain.model

// API 명세(DTO)와 독립적이며, UI 상태(isEditing 등)가 전혀 없는 순수 도메인 모델
data class OwnerSignUpInfo(
    val account: AccountInfo,
    val business: BusinessInfo,
    val layout: List<LayoutInfo>,
    val operation: OperationInfo
)

data class AccountInfo(val email: String, val password: String, val phoneNumber: String)

data class BusinessInfo(
    val representativeName: String, val businessNumber: String, val storeName: String,
    val address: String, val neighborhood: String, val latitude: Double,
    val longitude: Double, val universityNames: List<String>, val storePhone: String
)

data class LayoutInfo(val name: String, val tables: List<TableDetail>)

data class TableDetail(val tableType: Int, val tableCount: Int)

data class OperationInfo(
    val regularHolidays: List<RegularHolidayInfo>,
    val temporaryHolidays: List<TemporaryHolidayInfo>,
    val hours: List<OperatingHoursInfo>
)

data class RegularHolidayInfo(val dayOfWeek: String, val weekInfo: Int)

data class TemporaryHolidayInfo(val startDate: String, val endDate: String)

data class OperatingHoursInfo(val dayOfWeek: String, val startTime: String, val endTime: String)