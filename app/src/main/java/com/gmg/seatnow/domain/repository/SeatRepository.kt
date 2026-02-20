package com.gmg.seatnow.domain.repository

import com.gmg.seatnow.domain.model.FloorCategory
import com.gmg.seatnow.domain.model.TableItem


data class SeatStatusData(
    val categories: List<FloorCategory>,
    val allTables: List<TableItem>
)

// 좌석 관리 관련 데이터 통신 인터페이스
interface SeatRepository {
    // 좌석 사용 현황 업데이트 (API 호출)
    suspend fun getSeatStatus(forceRefresh: Boolean = false): Result<SeatStatusData>
    suspend fun updateSeatUsage(items: List<TableItem>): Result<Unit>
    suspend fun updateStoreLayout(spaces: List<com.gmg.seatnow.domain.model.SpaceItem>): Result<Unit>
}
