package com.gmg.seatnow.data.model.response

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

// 스웨거의 400, 500 에러 응답 구조에 맞춘 DTO
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class ErrorResponse(
    @SerializedName("code") val code: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("detail") val detail: String?
)