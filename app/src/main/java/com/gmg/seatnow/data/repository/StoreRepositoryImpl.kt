package com.gmg.seatnow.data.repository

import com.gmg.seatnow.data.api.AuthService
import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.MenuItemUiModel
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
}