package com.gmg.seatnow.data.model.request

import com.google.gson.annotations.SerializedName

// SMS 인증번호 검증 요청 DTO
data class EmailVerificationConfirmRequestDTO(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String
)