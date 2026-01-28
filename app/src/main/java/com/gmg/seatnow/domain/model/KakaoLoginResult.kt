package com.gmg.seatnow.domain.model

data class KakaoLoginResult(
    val accessToken: String,
    val refreshToken: String,
    val userId: Long,
    val storeId: Long?
)