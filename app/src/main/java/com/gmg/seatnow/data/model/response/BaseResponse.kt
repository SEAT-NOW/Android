package com.gmg.seatnow.data.model.response

import com.google.gson.annotations.SerializedName

// 스웨거의 공통 응답 포맷을 처리하는 Wrapper 클래스
data class BaseResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: T?,
    @SerializedName("message") val message: String?,
    @SerializedName("code") val code: String? // 에러 발생 시 코드
)