package com.gmg.seatnow.data.model.response

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class StoreMapResponseDTO(
    @SerialName("storeId") val storeId: Long,
    @SerialName("storeName") val storeName: String,
    @SerialName("neighborhood") val neighborhood: String?,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("totalSeatCount") val totalSeatCount: Int,
    @SerialName("availableSeatCount") val availableSeatCount: Int,
    @SerialName("statusTag") val statusTag: String?,
    @SerialName("statusTagName") val statusTagName: String?,
    @SerialName("images") val images: List<String>?,
    @SerialName("updatedAt") val updatedAt: String?,
    @SerialName("distance") val distance: String?,
    @SerialName("operationStatus") val operationStatus: String?,
    @SerialName("storePhone") val storePhone: String?
)