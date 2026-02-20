package com.gmg.seatnow.data.model.request

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class OwnerSignUpRequestDTO(
    @SerializedName("account") val account: AccountDTO,
    @SerializedName("business") val business: BusinessDTO,
    @SerializedName("layout") val layout: List<LayoutDTO>,
    @SerializedName("operation") val operation: OperationDTO
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class AccountDTO(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("phoneNumber") val phoneNumber: String
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class BusinessDTO(
    @SerializedName("representativeName") val representativeName: String,
    @SerializedName("businessNumber") val businessNumber: String,
    @SerializedName("storeName") val storeName: String,
    @SerializedName("address") val address: String,
    @SerializedName("neighborhood") val neighborhood: String, // 예: "역북동" (주소 파싱 필요 시 로직 추가, 여기선 임의값)
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("universityNames") val universityNames: List<String>,
    @SerializedName("storePhone") val storePhone: String
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class LayoutDTO(
    @SerializedName("name") val name: String, // 예: "1층 메인홀"
    @SerializedName("tables") val tables: List<TableInfoDTO>
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class TableInfoDTO(
    @SerializedName("tableType") val tableType: Int, // N인석 (예: 2, 4)
    @SerializedName("tableCount") val tableCount: Int
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class OperationDTO(
    @SerializedName("regularHolidays") val regularHolidays: List<RegularHolidayDTO>,
    @SerializedName("temporaryHolidays") val temporaryHolidays: List<TemporaryHolidayDTO>,
    @SerializedName("hours") val hours: List<OperatingHoursDTO>
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class RegularHolidayDTO(
    @SerializedName("dayOfWeek") val dayOfWeek: String, // "MONDAY"
    @SerializedName("weekInfo") val weekInfo: Int // 0: 매주, 1~5: 특정 주
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class TemporaryHolidayDTO(
    @SerializedName("startDate") val startDate: String, // "YYYY-MM-DD"
    @SerializedName("endDate") val endDate: String
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class OperatingHoursDTO(
    @SerializedName("dayOfWeek") val dayOfWeek: String, // "TUESDAY"
    @SerializedName("startTime") val startTime: String, // "10:00"
    @SerializedName("endTime") val endTime: String      // "22:00"
)