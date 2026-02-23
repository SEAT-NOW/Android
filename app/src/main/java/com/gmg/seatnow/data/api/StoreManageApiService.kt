// 경로: com.gmg.seatnow.data.api.StoreManageApiService.kt
package com.gmg.seatnow.data.api

import com.gmg.seatnow.data.model.request.*
import com.gmg.seatnow.data.model.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface StoreManageApiService {

    // ==========================================
    // 1. 매장 기본 정보 및 운영 관리
    // ==========================================
    @GET("/api/v1/stores/owner/profile")
    suspend fun getStoreProfile(): Response<BaseResponse<StoreProfileResponseDTO>>

    @PATCH("/api/v1/stores/operation/phone-number")
    suspend fun updateStorePhone(
        @Body request: StorePhoneUpdateRequestDTO
    ): Response<BaseResponse<Boolean>>

    @GET("/api/v1/stores/operation")
    suspend fun getStoreOperations(): Response<BaseResponse<StoreOperationResponse>>

    @PATCH("/api/v1/stores/operation")
    suspend fun updateStoreOperation(
        @Body request: StoreOperationRequest
    ): Response<BaseResponse<Boolean?>>

    @GET("/api/v1/stores/owner/account")
    suspend fun getOwnerAccount(): Response<BaseResponse<OwnerAccountResponseDTO>>

    // ==========================================
    // 2. 매장 사진 관리
    // ==========================================
    @GET("/api/v1/stores/operation/images")
    suspend fun getStoreImages(): Response<BaseResponse<StoreImageResponse>>

    @Multipart
    @PATCH("/api/v1/stores/operation/images")
    suspend fun updateStoreImages(
        @Part("updateData") updateData: RequestBody,
        @Part newImages: List<MultipartBody.Part>
    ): Response<BaseResponse<Boolean?>>

    // ==========================================
    // 3. 메뉴 관리
    // ==========================================
    @GET("/api/v1/stores/menus")
    suspend fun getStoreMenus(): Response<BaseResponse<StoreMenuResponseDTO>>

    @PATCH("/api/v1/stores/menus/categories")
    suspend fun updateMenuCategories(
        @Body request: UpdateMenuCategoriesRequest
    ): Response<BaseResponse<Boolean>>

    @Multipart
    @POST("/api/v1/stores/menus")
    suspend fun saveMenu(
        @Part("menuData") menuData: RequestBody,
        @Part menuImage: MultipartBody.Part?
    ): Response<BaseResponse<Boolean>>

    @PATCH("/api/v1/stores/menus/order")
    suspend fun updateMenuOrders(
        @Body request: MenuOrderRequest
    ): Response<BaseResponse<Boolean>>

    @DELETE("/api/v1/stores/menus/{menuId}")
    suspend fun deleteMenu(
        @Path("menuId") menuId: Long
    ): Response<BaseResponse<Boolean>>

    // ==========================================
    // 4. 좌석 및 레이아웃 관리 (SeatApiService 병합)
    // ==========================================
    @GET("/api/v1/stores/{storeId}/seats")
    suspend fun getSeatStatus(
        @Path("storeId") storeId: Long
    ): Response<BaseResponse<SeatStatusResponseDTO>>

    @PATCH("/api/v1/stores/seats")
    suspend fun updateSeatStatus(
        @Body request: SeatUpdateRequestDTO
    ): Response<BaseResponse<SeatStatusResponseDTO>>

    @PATCH("/api/v1/stores/layout")
    suspend fun updateStoreLayout(
        @Body request: List<SpaceLayoutUpdateRequest>
    ): Response<BaseResponse<Boolean>>
}