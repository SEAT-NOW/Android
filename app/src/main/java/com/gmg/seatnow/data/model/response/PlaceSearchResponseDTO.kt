package com.gmg.seatnow.data.model.response

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class PlaceSearchResponseDTO(
    @SerializedName("name") val name: String,
    @SerializedName("roadAddress") val roadAddress: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)