package com.gmg.seatnow.data.api

import com.gmg.seatnow.data.model.response.BaseResponse
import com.gmg.seatnow.data.model.response.StoreDetailResponse
import com.gmg.seatnow.data.model.response.StoreKeptResponseDTO
import com.gmg.seatnow.data.model.response.StoreMapResponseDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApiService {

    // [메인 홈] 지도상의 가게 검색
    @GET("/api/v1/stores/search")
    suspend fun getStoresOnMap(
        @Query("keyword") keyword: String?,
        // ★ 여기가 minPerson이 아니라 "headCount"여야 합니다!
        @Query("headCount") headCount: Int?,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Double
    ): Response<BaseResponse<List<StoreMapResponseDTO>>>

    // [상세 조회] 반환 타입을 StoreDetailResponse로 변경
    @GET("/api/v1/stores/details/{storeId}")
    suspend fun getStoreDetail(
        @Path("storeId") storeId: Long
    ): Response<BaseResponse<StoreDetailResponse>>

    // [추가됨] 매장 스크랩 (킵 하기/취소하기)
    // 토글 방식이라면 POST 호출 시 상태 변경, 혹은 DELETE 별도 존재 가능
    @POST("/api/v1/stores/scraps/{storeId}")
    suspend fun scrapStore(
        @Path("storeId") storeId: Long
    ): Response<BaseResponse<Unit>> // 데이터가 없으면 Unit

    // 킵한 매장 조회
    @GET("/api/v1/stores/kept")
    suspend fun getKeptStores(): Response<BaseResponse<List<StoreKeptResponseDTO>>>

    // 킵 저장
    @POST("/api/v1/stores/{storeId}/keep")
    suspend fun keepStore(
        @Path("storeId") storeId: Long
    ): Response<BaseResponse<Boolean>>
}