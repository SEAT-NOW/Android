package com.gmg.seatnow.data.model.response

import com.google.gson.annotations.SerializedName

data class StoreImageResponse(
    @SerializedName("storeImages") val storeImages: List<StoreImageDTO>
)

data class StoreImageDTO(
    val id: Long,
    val imageUrl: String,
    val main: Boolean
)