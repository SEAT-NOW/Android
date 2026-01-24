package com.gmg.seatnow.data.model.response

import com.gmg.seatnow.domain.model.KakaoLoginResult

data class KakaoLoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: Long,
    val storeId: Long?
) {
    // Impl에서 매핑 오류가 나지 않도록 여기서 변환을 담당합니다.
    fun toDomain(appAccessToken: String) = KakaoLoginResult(
        accessToken = appAccessToken,
        refreshToken = refreshToken,
        userId = userId,
        storeId = storeId
    )
}