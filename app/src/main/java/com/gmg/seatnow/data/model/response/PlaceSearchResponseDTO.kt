package com.gmg.seatnow.data.model.response

import com.google.gson.annotations.SerializedName

data class PlaceSearchResponseDTO(
    @SerializedName("name") val name: String,
    @SerializedName("roadAddress") val roadAddress: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double
)