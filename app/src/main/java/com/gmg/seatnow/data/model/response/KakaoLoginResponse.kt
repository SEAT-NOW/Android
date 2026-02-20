package com.gmg.seatnow.data.model.response

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.gmg.seatnow.domain.model.KakaoLoginResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class KakaoLoginResponse(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("refreshToken") val refreshToken: String,
    @SerialName("userId") val userId: Long,
    @SerialName("storeId") val storeId: Long?
) {
    // Impl에서 매핑 오류가 나지 않도록 여기서 변환을 담당합니다.
    fun toDomain(appAccessToken: String) = KakaoLoginResult(
        accessToken = appAccessToken,
        refreshToken = refreshToken,
        userId = userId,
        storeId = storeId
    )
}