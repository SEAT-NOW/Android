package com.gmg.seatnow.data.model.response

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class BaseResponse<T>(
    @SerialName("success") val success: Boolean,

    // data도 null일 수 있으므로 기본값 null 처리
    @SerialName("data") val data: T? = null,

    @SerialName("message") val message: String? = null,

    // ★ 에러 원인 해결: 기본값(= null)을 주면 JSON에 'code'가 없어도 에러 안 남
    @SerialName("code") val code: String? = null
)