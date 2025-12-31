package com.gmg.seatnow.data.api

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// 서버 응답 모델 (예시: 이미지 URL을 반환한다고 가정)
data class ImageUploadResponse(
    val imageUrl: String
)

interface FileApiService {
    @Multipart
    @POST("api/v1/upload/image") // ★ 실제 백엔드 API 경로로 수정 필요
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): ImageUploadResponse
}