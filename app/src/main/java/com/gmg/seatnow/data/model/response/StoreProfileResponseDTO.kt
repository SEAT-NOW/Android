package com.gmg.seatnow.data.model.response

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class StoreProfileResponseDTO(
    @SerializedName("representativeName") val representativeName: String,
    @SerializedName("businessNumber") val businessNumber: String,
    @SerializedName("storeName") val storeName: String,
    @SerializedName("address") val address: String,
    @SerializedName("universityNames") val universityNames: List<String>?, // 리스트 형태
    @SerializedName("businessLicenseFileName") val businessLicenseFileName: String?,
    @SerializedName("storePhone") val storePhone: String?
)