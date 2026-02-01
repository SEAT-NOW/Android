package com.gmg.seatnow.data.model.request

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

// 스웨거 Request Body
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class SmsVerificationRequestDTO(
    @SerializedName("phoneNumber") val phoneNumber: String
)

