package com.gmg.seatnow.domain.usecase.owner.seat

import com.gmg.seatnow.domain.model.SignUpTableItem
import javax.inject.Inject

class CalculateSeatCountUseCase @Inject constructor() {
    operator fun invoke(tableList: List<SignUpTableItem>): Int {
        // 총 좌석 수 = (테이블 당 인원 x 테이블 개수)의 총합
        return tableList.sumOf { 
            (it.personCount.toIntOrNull() ?: 0) * (it.tableCount.toIntOrNull() ?: 0) 
        }
    }
}