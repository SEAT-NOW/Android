package com.gmg.seatnow.data.repository

import android.util.Log
import com.gmg.seatnow.data.api.AuthService
import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.data.model.request.SeatUpdateRequestDTO
import com.gmg.seatnow.data.model.request.SpaceLayoutUpdateRequest
import com.gmg.seatnow.data.model.request.SpaceUpdateDTO
import com.gmg.seatnow.data.model.request.TableLayoutUpdateRequest
import com.gmg.seatnow.data.model.request.TableUpdateDTO
import com.gmg.seatnow.data.model.response.ErrorResponse
import com.gmg.seatnow.domain.model.FloorCategory
import com.gmg.seatnow.domain.model.SpaceItem
import com.gmg.seatnow.domain.model.TableItem
import com.gmg.seatnow.domain.repository.SeatRepository
import com.gmg.seatnow.domain.repository.SeatStatusData
import com.google.gson.Gson
import kotlinx.coroutines.delay
import javax.inject.Inject

class SeatRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val authManager: AuthManager
) : SeatRepository {

    // 메모리 캐시 (화면 이동 간 데이터 유지용)
    private var cachedSeatData: SeatStatusData? = null

    override suspend fun getSeatStatus(forceRefresh: Boolean): Result<SeatStatusData> {
        // 강제 새로고침이 아니고 캐시가 있으면 캐시 반환 (화면 이동 시 깜빡임 방지)
        if (!forceRefresh && cachedSeatData != null) {
            return Result.success(cachedSeatData!!)
        }

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
                            name = space.name ?: "공간 ${space.spaceId}"
                        )
                    })

                    val allTables = spaces.flatMap { space ->
                        val tables = space.tables ?: emptyList()
                        tables.map { tableDto ->
                            TableItem(
                                id = tableDto.tableId.toString(),
                                floorId = space.spaceId.toString(),
                                label = "${tableDto.seatCount}인 테이블",
                                capacityPerTable = tableDto.seatCount,
                                maxTableCount = tableDto.totalCount,
                                currentCount = tableDto.usedCount
                            )
                        }
                    }

                    val resultData = SeatStatusData(categories, allTables)

                    // 조회 성공 시 캐시 최신화
                    cachedSeatData = resultData

                    Result.success(resultData)
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
            val groupedByFloor = items.groupBy { it.floorId }

            val spaceUpdates = groupedByFloor.map { (spaceIdStr, tables) ->
                SpaceUpdateDTO(
                    spaceId = spaceIdStr.toLong(),
                    tableUpdates = tables.map { table ->
                        TableUpdateDTO(
                            tableConfigId = table.id.toLong(),
                            usedCount = table.currentCount
                        )
                    }
                )
            }

            val requestDto = SeatUpdateRequestDTO(
                storeId = storeId,
                spaceUpdates = spaceUpdates
            )

            val response = authService.updateSeatStatus(requestDto)

            if (response.isSuccessful && response.body()?.success == true) {

                // ★ [핵심 수정] API 성공 시 로컬 캐시(cachedSeatData)도 같이 업데이트해줍니다.
                // 그래야 탭을 이동했다가 돌아왔을 때 서버 재조회 없이도 최신 데이터가 보입니다.
                cachedSeatData?.let { currentCache ->
                    val updatedTableMap = items.associateBy { it.id }

                    // 기존 캐시의 테이블 리스트를 순회하며, 수정된 아이템이 있으면 교체
                    val newAllTables = currentCache.allTables.map { existingTable ->
                        updatedTableMap[existingTable.id]?.let { updatedItem ->
                            // 업데이트된 사용량(currentCount) 반영
                            existingTable.copy(currentCount = updatedItem.currentCount)
                        } ?: existingTable
                    }

                    // 캐시 갱신
                    cachedSeatData = currentCache.copy(allTables = newAllTables)
                }

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

    override suspend fun updateStoreLayout(spaces: List<SpaceItem>): Result<Unit> {
        return try {
            val requestDto = spaces.map { space ->
                SpaceLayoutUpdateRequest(
                    // ★ [수정] ID가 0보다 작으면(음수) 신규 생성이므로 null을 보냅니다.
                    // 기존 ID(양수)는 그대로 보냅니다.
                    id = if (space.id < 0) null else space.id,
                    name = space.name,
                    tables = space.tableList.map { table ->
                        TableLayoutUpdateRequest(
                            // ★ [수정] 테이블 ID도 마찬가지로 음수면 null로 보냅니다.
                            tableConfigId = if (table.id < 0) null else table.id,
                            tableType = table.personCount.toIntOrNull() ?: 0,
                            tableCount = table.tableCount.toIntOrNull() ?: 0
                        )
                    }
                )
            }

            val response = authService.updateStoreLayout(requestDto)

            if (response.isSuccessful && response.body()?.success == true) {
                // ★ [중요] 구조가 변경되면(추가/삭제) 서버에서 새로운 ID가 발급됩니다.
                // 현재 클라이언트가 가진 음수 ID는 더 이상 유효하지 않으므로
                // 캐시를 비워버려서 다음 조회 때 서버에서 최신 데이터(진짜 ID)를 받아오도록 강제합니다.
                cachedSeatData = null

                Result.success(Unit)
            } else {
                Result.failure(Exception("수정 실패: ${response.message()}"))
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