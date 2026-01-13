package com.gmg.seatnow.data.model.request

import com.google.gson.annotations.SerializedName

// 스웨거 Request Body
data class EmailVerificationRequestDTO(
    @SerializedName("email") val email: String
)