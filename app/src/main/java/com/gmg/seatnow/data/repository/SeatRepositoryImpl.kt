package com.gmg.seatnow.data.repository

import android.util.Log
import com.gmg.seatnow.domain.model.TableItem
import com.gmg.seatnow.domain.repository.SeatRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class SeatRepositoryImpl @Inject constructor(
    // private val api: SeatNowApi // 나중에 실제 API 서비스가 주입될 곳
) : SeatRepository {

    override suspend fun updateSeatUsage(items: List<TableItem>): Result<Unit> {
        return try {
            // [Mocking] 실제 네트워크 통신인 척 1초 대기
            delay(1000)

            // [Mocking] 서버로 보내는 데이터를 로그로 확인
            Log.d("SeatRepository", "=== API 호출: 좌석 정보 업데이트 ===")
            items.forEach { item ->
                Log.d("SeatRepository", "ID: ${item.id}, Label: ${item.label}, Current: ${item.currentCount}/${item.maxTableCount}")
            }
            Log.d("SeatRepository", "=================================")

            // 성공 반환 (실제로는 api.updateSeats(dto) 호출 결과)
            Result.success(Unit)
        } catch (e: Exception) {
            // 실패 시
            e.printStackTrace()
            Result.failure(e)
        }
    }
}