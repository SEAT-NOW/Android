package com.gmg.seatnow.data.repository

import android.util.Log
import com.gmg.seatnow.data.api.AuthService
import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.domain.model.FloorCategory
import com.gmg.seatnow.domain.model.TableItem
import com.gmg.seatnow.domain.repository.SeatRepository
import com.gmg.seatnow.domain.repository.SeatStatusData
import kotlinx.coroutines.delay
import javax.inject.Inject

class SeatRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val authManager: AuthManager // 나중에 실제 API 서비스가 주입될 곳
) : SeatRepository {

    override suspend fun getSeatStatus(): Result<SeatStatusData> {
        val storeId = authManager.getStoreId()
        if (storeId == -1L) {
            return Result.failure(Exception("가게 정보를 찾을 수 없습니다. 다시 로그인해주세요."))
        }

        return try {
            val response = authService.getSeatStatus(storeId)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data

                if (data != null) {
                    val categories = mutableListOf(FloorCategory("ALL", "전체"))
                    val spaces = data.spaces ?: emptyList()

                    categories.addAll(spaces.map { space ->
                        FloorCategory(
                            id = space.spaceId.toString(),
                            // "1" -> "1층", "2" -> "2층" 등으로 변환하고 싶으면 여기서 처리 가능
                            // 현재는 서버 값 그대로 "1", "2" 등을 사용
                            name = space.name ?: "공간 ${space.spaceId}"
                        )
                    })

                    val allTables = spaces.flatMap { space ->
                        val tables = space.tables ?: emptyList()
                        tables.map { tableDto ->
                            TableItem(
                                id = tableDto.tableId.toString(),
                                floorId = space.spaceId.toString(),
                                // ★ [수정] 서버에 name 필드가 없으므로 seatCount(tableType)를 이용해 라벨 생성
                                label = "${tableDto.seatCount}인 테이블",
                                capacityPerTable = tableDto.seatCount,
                                maxTableCount = tableDto.totalCount,
                                currentCount = tableDto.usedCount
                            )
                        }
                    }

                    Result.success(SeatStatusData(categories, allTables))
                } else {
                    Result.failure(Exception("데이터가 비어있습니다."))
                }
            } else {
                val errorMsg = response.errorBody()?.string() ?: "조회 실패"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

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