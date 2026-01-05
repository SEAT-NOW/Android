package com.gmg.seatnow.domain.repository

import com.gmg.seatnow.domain.model.TableItem

// 좌석 관리 관련 데이터 통신 인터페이스
interface SeatRepository {
    // 좌석 사용 현황 업데이트 (API 호출)
    suspend fun updateSeatUsage(items: List<TableItem>): Result<Unit>
}