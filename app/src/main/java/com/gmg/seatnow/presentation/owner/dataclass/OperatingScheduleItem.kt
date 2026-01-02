package com.gmg.seatnow.presentation.owner.dataclass

data class OperatingScheduleItem(
        val id: Long = System.currentTimeMillis(),
        val selectedDays: Set<Int> = emptySet(), // 0:일, 1:월 ...
        val startHour: Int = 9,
        val startMin: Int = 0,
        val endHour: Int = 22,
        val endMin: Int = 0
    )