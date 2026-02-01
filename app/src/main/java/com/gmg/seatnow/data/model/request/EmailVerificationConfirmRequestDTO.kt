package com.gmg.seatnow.data.model.request

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

// SMS 인증번호 검증 요청 DTO
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class EmailVerificationConfirmRequestDTO(
    @SerializedName("email") val email: String,
    @SerializedName("code") val code: String
)