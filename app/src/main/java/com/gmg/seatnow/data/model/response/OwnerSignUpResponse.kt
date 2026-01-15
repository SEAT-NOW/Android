package com.gmg.seatnow.data.model.response

import com.google.gson.annotations.SerializedName

data class OwnerSignUpResponse(
    @SerializedName("storeId") val storeId: Int
)