package com.gmg.seatnow.data.model.response

import com.google.gson.annotations.SerializedName

data class StoreMapResponseDTO(
    @SerializedName("storeId") val storeId: Long,
    @SerializedName("storeName") val storeName: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("statusTag") val statusTag: String?, // "CROWDED", "SPACIOUS" 등
    @SerializedName("availableSeatCount") val availableSeatCount: Int
)