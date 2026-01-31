package com.gmg.seatnow.data.repository

import com.gmg.seatnow.data.api.AuthService
import com.gmg.seatnow.data.model.request.HourRequest
import com.gmg.seatnow.data.model.request.RegularHolidayRequest
import com.gmg.seatnow.data.model.request.StoreOperationRequest
import com.gmg.seatnow.data.model.request.TemporaryHolidayRequest
import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.MenuItemUiModel
import com.gmg.seatnow.domain.model.OpeningHour
import com.gmg.seatnow.domain.model.RegularHoliday
import com.gmg.seatnow.domain.model.StoreOperationInfo
import com.gmg.seatnow.domain.model.TemporaryHoliday
import com.gmg.seatnow.domain.repository.StoreRepository
import javax.inject.Inject

class StoreRepositoryImpl @Inject constructor(
    private val authService: AuthService
) : StoreRepository {

    // ★ 메모리 캐싱 변수
    private var cachedMenus: List<MenuCategoryUiModel>? = null

    override suspend fun getStoreMenus(forceRefresh: Boolean): Result<List<MenuCategoryUiModel>> {
        // 1. 캐시 확인
        if (!forceRefresh && cachedMenus != null) {
            return Result.success(cachedMenus!!)
        }

        return try {
            // 2. API 호출
            val response = authService.getStoreMenus()

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data

                // 3. DTO -> Domain Model 변환
                val mappedData = data?.categories?.map { category ->
                    MenuCategoryUiModel(
                        categoryName = category.name,
                        menuItems = category.menus?.map { menu ->
                            MenuItemUiModel(
                                id = menu.id,
                                name = menu.name,
                                price = menu.price,
                                imageUrl = menu.imageUrl ?: "", // 이미지가 없으면 빈 문자열
                                isRecommended = false, // API에 없으므로 기본값
                                isLiked = false        // 사장님 앱에서는 불필요하므로 false
                            )
                        } ?: emptyList()
                    )
                } ?: emptyList()

                // 4. 캐시 저장 및 반환
                cachedMenus = mappedData
                Result.success(mappedData)
            } else {
                Result.failure(Exception("메뉴 조회 실패"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getStoreOperations(): Result<StoreOperationInfo> {
        return try {
            val response = authService.getStoreOperations()
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    // DTO -> Domain Model 변환 (Mapper 로직)
                    val info = StoreOperationInfo(
                        operationStatus = data.operationStatus ?: "CLOSED",
                        regularHolidays = data.regularHolidays.map {
                            RegularHoliday(it.dayOfWeek, it.weekInfo)
                        },
                        temporaryHolidays = data.temporaryHolidays.map {
                            TemporaryHoliday(it.startDate, it.endDate)
                        },
                        openingHours = data.openingHours.map {
                            OpeningHour(it.dayOfWeek, it.startTime, it.endTime)
                        }
                    )
                    Result.success(info)
                } else {
                    Result.failure(Exception("데이터가 없습니다."))
                }
            } else {
                Result.failure(Exception("운영 정보 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStoreImages(): Result<List<String>> {
        return try {
            val response = authService.getStoreImages()
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    // 메인 사진이 있다면 맨 앞으로, 나머지는 뒤로 정렬하여 URL 리스트 반환
                    val sortedImages = data.storeImages
                        .sortedByDescending { it.main }
                        .map { it.imageUrl }

                    Result.success(sortedImages)
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(Exception("사진 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStoreOperation(
        regularHolidays: List<RegularHoliday>,
        temporaryHolidays: List<TemporaryHoliday>,
        openingHours: List<OpeningHour>
    ): Result<Unit> {
        return try {
            // Domain Model -> Request DTO 변환
            // (ID를 null로 보내어 기존 데이터를 덮어쓰거나(삭제 후 생성) 처리 유도)
            val request = StoreOperationRequest(
                regularHolidays = regularHolidays.map {
                    RegularHolidayRequest(dayOfWeek = it.dayOfWeek, weekInfo = it.weekInfo)
                },
                temporaryHolidays = temporaryHolidays.map {
                    TemporaryHolidayRequest(startDate = it.startDate, endDate = it.endDate)
                },
                hours = openingHours.map {
                    HourRequest(dayOfWeek = it.dayOfWeek, startTime = it.startTime, endTime = it.endTime)
                }
            )

            val response = authService.updateStoreOperation(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "수정 실패"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}