package com.gmg.seatnow.data.model.response

import com.google.gson.annotations.SerializedName

data class OwnerLoginResponseDTO(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)