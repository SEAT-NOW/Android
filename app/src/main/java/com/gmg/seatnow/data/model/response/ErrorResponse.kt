package com.gmg.seatnow.data.model.response

import com.google.gson.annotations.SerializedName

// 스웨거의 400, 500 에러 응답 구조에 맞춘 DTO
data class ErrorResponse(
    @SerializedName("code") val code: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("detail") val detail: String?
)