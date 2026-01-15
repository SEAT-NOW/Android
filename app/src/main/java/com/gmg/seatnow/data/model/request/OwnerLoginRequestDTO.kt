package com.gmg.seatnow.data.model.request

import com.google.gson.annotations.SerializedName

data class OwnerLoginRequestDTO(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)