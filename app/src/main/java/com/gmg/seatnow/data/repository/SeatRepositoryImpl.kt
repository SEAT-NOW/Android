package com.gmg.seatnow.data.repository

import android.util.Log
import com.gmg.seatnow.data.api.AuthService
import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.data.model.request.SeatUpdateRequestDTO
import com.gmg.seatnow.data.model.request.SpaceUpdateDTO
import com.gmg.seatnow.data.model.request.TableUpdateDTO
import com.gmg.seatnow.data.model.response.ErrorResponse
import com.gmg.seatnow.domain.model.FloorCategory
import com.gmg.seatnow.domain.model.TableItem
import com.gmg.seatnow.domain.repository.SeatRepository
import com.gmg.seatnow.domain.repository.SeatStatusData
import com.google.gson.Gson
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
        val storeId = authManager.getStoreId()
        if (storeId == -1L) return Result.failure(Exception("로그인 정보가 없습니다."))

        return try {
            // 1. 전달받은 TableItem 리스트를 floorId(spaceId)별로 그룹화
            // (ViewModel에서 이미 1개 층만 보냈다면 그룹은 1개만 생성됨)
            val groupedByFloor = items.groupBy { it.floorId }

            // 2. DTO 변환
            val spaceUpdates = groupedByFloor.map { (spaceIdStr, tables) ->
                SpaceUpdateDTO(
                    spaceId = spaceIdStr.toLong(),
                    tableUpdates = tables.map { table ->
                        TableUpdateDTO(
                            tableConfigId = table.id.toLong(),
                            usedCount = table.currentCount // ★ 무조건 이용중인 개수 전송
                        )
                    }
                )
            }

            // 3. Request Body 생성
            val requestDto = SeatUpdateRequestDTO(
                storeId = storeId,
                spaceUpdates = spaceUpdates
            )

            // 4. API 호출
            val response = authService.updateSeatStatus(requestDto)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            errorResponse.message ?: errorResponse.detail ?: "알 수 없는 오류가 발생했습니다."
        } catch (e: Exception) {
            "서버 통신 오류가 발생했습니다."
        }
    }
}