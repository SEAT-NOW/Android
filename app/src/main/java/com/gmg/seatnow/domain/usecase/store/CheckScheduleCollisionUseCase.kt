package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.model.OperatingScheduleItem
import javax.inject.Inject

class CheckScheduleCollisionUseCase @Inject constructor() {
    operator fun invoke(currentSchedules: List<OperatingScheduleItem>, targetId: Long, dayIdx: Int): Boolean {
        // 다른 스케줄 아이템에서 이미 해당 요일을 점유하고 있는지 확인
        return currentSchedules.any { item ->
            item.id != targetId && item.selectedDays.contains(dayIdx)
        }
    }
}