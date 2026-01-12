package com.gmg.seatnow.data.model.request

import com.google.gson.annotations.SerializedName

// 스웨거 Request Body
data class SmsVerificationRequest(
    @SerializedName("phoneNumber") val phoneNumber: String
)