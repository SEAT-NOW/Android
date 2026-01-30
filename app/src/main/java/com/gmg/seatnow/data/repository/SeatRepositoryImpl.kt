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
    private val authManager: AuthManager // 나중에 실제 API 서비스가 주입될 곳
) : SeatRepository {
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
                    // --- [기존 로직 유지] ---
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
                    // -----------------------

                    val resultData = SeatStatusData(categories, allTables)

                    // ★ [추가] 조회 성공 시 캐시에 저장
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

    override suspend fun updateStoreLayout(spaces: List<SpaceItem>): Result<Unit> {
        return try {
            // 1) Request DTO 변환 (서버 전송용)
            val requestDto = spaces.map { space ->
                SpaceLayoutUpdateRequest(
                    // ID가 임시값(너무 큰 숫자)이면 신규 생성이므로 null 처리 (서버 규칙에 따름)
                    // 기존 데이터라면 ID 유지
                    id = if (space.id > 1000000000) null else space.id,
                    name = space.name,
                    tables = space.tableList.map { table ->
                        TableLayoutUpdateRequest(
                            tableConfigId = if (table.id > 1000000000) null else table.id,
                            tableType = table.personCount.toIntOrNull() ?: 0,
                            tableCount = table.tableCount.toIntOrNull() ?: 0
                        )
                    }
                )
            }

            // 2) API 호출
            val response = authService.updateStoreLayout(requestDto)

            if (response.isSuccessful && response.body()?.success == true) {

                // ★ 3) 성공 시 [낙관적 업데이트]: 로컬 캐시를 내가 수정한 데이터로 덮어씌움
                // 이렇게 하면 SeatManagementScreen에서 재조회(API) 없이도 바뀐 화면이 나옴

                // 3-1. 카테고리 구성 (기존 로직처럼 "ALL" 추가 필수)
                val newCategories = mutableListOf(FloorCategory("ALL", "전체"))
                newCategories.addAll(spaces.map {
                    FloorCategory(it.id.toString(), it.name)
                })

                // 3-2. 테이블 리스트 구성
                val newAllTables = spaces.flatMap { space ->
                    space.tableList.map { table ->
                        TableItem(
                            id = table.id.toString(),
                            floorId = space.id.toString(),
                            // 기존 로직과 동일한 포맷 유지
                            label = "${table.personCount}인 테이블",
                            capacityPerTable = table.personCount.toIntOrNull() ?: 0,
                            maxTableCount = table.tableCount.toIntOrNull() ?: 0,
                            // 구성 변경 시 사용 중 좌석은 0으로 초기화된다고 가정 (서버 로직에 따름)
                            currentCount = 0
                        )
                    }
                }

                // 3-3. 캐시 갱신!
                cachedSeatData = SeatStatusData(newCategories, newAllTables)

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