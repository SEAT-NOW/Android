package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.data.model.response.OwnerAccountResponseDTO
import com.gmg.seatnow.domain.model.OpeningHour
import com.gmg.seatnow.domain.model.RegularHoliday
import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.data.model.response.StoreProfileResponseDTO
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.domain.model.StoreOperationInfo
import com.gmg.seatnow.domain.repository.SeatStatusData
import javax.inject.Inject

class FormatStoreDetailUseCase @Inject constructor() {

    data class Params(
        val storeData: StoreProfileResponseDTO,
        val ownerData: OwnerAccountResponseDTO?,
        val seatData: SeatStatusData?,
        val operationData: StoreOperationInfo?,
        val imagesData: List<String>
    )

    operator fun invoke(params: Params): StoreDetail {
        val storeData = params.storeData
        val ownerData = params.ownerData
        val seatData = params.seatData
        val operationData = params.operationData
        val imagesData = params.imagesData

        var totalSeats = 0
        var availableSeats = 0
        var calculatedStatus = StoreStatus.NORMAL

        if (seatData != null) {
            totalSeats = seatData.allTables.sumOf { it.capacityPerTable * it.maxTableCount }
            val usedSeats = seatData.allTables.sumOf { it.capacityPerTable * it.currentCount }
            availableSeats = (totalSeats - usedSeats).coerceAtLeast(0)
            calculatedStatus = calculateStoreStatus(totalSeats, usedSeats)
        }

        val finalPhoneNumber = if (!storeData.storePhone.isNullOrBlank()) {
            storeData.storePhone
        } else {
            ownerData?.phoneNumber ?: ""
        }

        val currentOperationStatus = if (operationData != null) {
            when (operationData.operationStatus) {
                "OPEN" -> "영업 중"
                "CLOSED" -> "영업 종료"
                "BREAK_TIME" -> "브레이크 타임"
                else -> "영업 정보 없음"
            }
        } else {
            "영업 정보 없음"
        }

        val formattedOpenHours = if (operationData != null) {
            formatOpenHours(operationData.openingHours)
        } else {
            "00:00 ~ 00:00"
        }

        val formattedClosedDays = if (operationData != null) {
            formatClosedDays(operationData.regularHolidays)
        } else {
            "연중무휴"
        }

        return StoreDetail(
            id = 0L,
            name = storeData.storeName,
            address = storeData.address,
            storePhone = finalPhoneNumber,
            universityInfo = storeData.universityNames?.joinToString(", ") ?: "",
            availableSeatCount = availableSeats,
            totalSeatCount = totalSeats,
            status = calculatedStatus,
            images = imagesData,
            operationStatus = currentOperationStatus,
            openHours = formattedOpenHours,
            closedDays = formattedClosedDays,
            isKept = false
        )
    }

    private fun calculateStoreStatus(total: Int, used: Int): StoreStatus {
        if (total == 0) return StoreStatus.NORMAL
        val occupancyRate = (used.toDouble() / total.toDouble()) * 100
        return when {
            occupancyRate >= 100f -> StoreStatus.FULL
            occupancyRate >= 67f -> StoreStatus.HARD
            occupancyRate >= 34f -> StoreStatus.NORMAL
            else -> StoreStatus.SPARE
        }
    }

    private fun formatOpenHours(openingHours: List<OpeningHour>): String {
        if (openingHours.isEmpty()) return "정보 없음"
        val grouped = openingHours.groupBy { "${it.startTime}~${it.endTime}" }
        return grouped.map { (timeRange, hoursList) ->
            val daysStr = hoursList
                .sortedBy { mapDayStringToInt(it.dayOfWeek) }
                .joinToString(", ") { mapDayStringToKorean(it.dayOfWeek) }
            val (start, end) = timeRange.split("~")
            val cleanStart = start.substringBeforeLast(":")
            val cleanEnd = end.substringBeforeLast(":")
            "$daysStr $cleanStart ~ $cleanEnd"
        }.joinToString("\n")
    }

    private fun formatClosedDays(holidays: List<RegularHoliday>): String {
        if (holidays.isEmpty()) return "연중무휴"
        val resultStrings = mutableListOf<String>()

        val weeklyHolidays = holidays.filter { it.weekInfo == 0 }
        if (weeklyHolidays.isNotEmpty()) {
            val days = weeklyHolidays
                .sortedBy { mapDayStringToInt(it.dayOfWeek) }
                .joinToString(", ") { mapDayStringToKorean(it.dayOfWeek) }
            resultStrings.add("매주 ${days}요일")
        }

        val monthlyHolidays = holidays.filter { it.weekInfo != 0 }
        val monthlyGroups = monthlyHolidays.groupBy { it.dayOfWeek }
        monthlyGroups.forEach { (dayOfWeek, list) ->
            val dayStr = mapDayStringToKorean(dayOfWeek)
            val weeksStr = list.map { it.weekInfo }
                .sorted()
                .joinToString(", ") { if (it == 10) "마지막" else "$it" }
            val prefix = if (weeksStr.contains("마지막")) "" else "주"
            resultStrings.add("$weeksStr$prefix ${dayStr}요일")
        }
        return resultStrings.joinToString(" / ") + " 휴무"
    }

    private fun mapDayStringToKorean(day: String): String {
        return when (day.uppercase()) {
            "MONDAY" -> "월"
            "TUESDAY" -> "화"
            "WEDNESDAY" -> "수"
            "THURSDAY" -> "목"
            "FRIDAY" -> "금"
            "SATURDAY" -> "토"
            "SUNDAY" -> "일"
            else -> ""
        }
    }

    private fun mapDayStringToInt(day: String): Int {
        return when (day.uppercase()) {
            "SUNDAY" -> 0
            "MONDAY" -> 1
            "TUESDAY" -> 2
            "WEDNESDAY" -> 3
            "THURSDAY" -> 4
            "FRIDAY" -> 5
            "SATURDAY" -> 6
            else -> 7
        }
    }
}
