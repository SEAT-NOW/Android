package com.gmg.seatnow.data.model.request

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class OwnerWithdrawRequestDTO(
    @SerializedName("businessNumber") val businessNumber: String,
    @SerializedName("password") val password: String
)