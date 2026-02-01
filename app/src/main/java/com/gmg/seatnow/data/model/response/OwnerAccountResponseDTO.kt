package com.gmg.seatnow.data.model.response

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class OwnerAccountResponseDTO(
    @SerialName("phoneNumber") val phoneNumber: String,
    @SerialName("email") val email: String
)