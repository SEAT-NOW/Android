package com.gmg.seatnow.data.model.request

import com.google.gson.annotations.SerializedName

// SMS 인증번호 검증 요청 DTO
data class BusinessVerificationConfirmRequestDTO(
    @SerializedName("businessNumber") val businessNumber: String
)