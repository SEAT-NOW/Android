package com.gmg.seatnow.data.model.response

import com.google.gson.annotations.SerializedName

data class StoreMapResponseDTO(
    @SerializedName("storeId") val storeId: Long,
    @SerializedName("storeName") val storeName: String,
    @SerializedName("neighborhood") val neighborhood: String?,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("totalSeatCount") val totalSeatCount: Int, // [추가]
    @SerializedName("availableSeatCount") val availableSeatCount: Int, // [추가]
    @SerializedName("statusTag") val statusTag: String?,
    @SerializedName("statusTagName") val statusTagName: String?, // [추가]
    @SerializedName("images") val images: List<String>?,
    @SerializedName("updatedAt") val updatedAt: String?, // [추가]
    @SerializedName("distance") val distance: String?,
    @SerializedName("operationStatus") val operationStatus: String?,
    @SerializedName("storePhone") val storePhone: String?

)