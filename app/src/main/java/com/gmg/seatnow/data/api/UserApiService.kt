package com.gmg.seatnow.data.api

import com.gmg.seatnow.data.model.response.BaseResponse
import com.gmg.seatnow.data.model.response.StoreMapResponseDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UserApiService {

    // [메인 홈] 지도상의 가게 검색 (핀 찍기용)
    // headCount: 0이면 전체 조회, 1 이상이면 필터링
    @GET("/api/v1/stores/search")
    suspend fun getStoresOnMap(
        @Query("keyword") keyword: String? = null,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("headCount") headCount: Int = 0,
        @Query("radius") radius: Double
    ): Response<BaseResponse<List<StoreMapResponseDTO>>>
}