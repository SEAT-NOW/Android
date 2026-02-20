package com.gmg.seatnow.data.model.response

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

// 1. 전체 응답 래퍼 (data 필드 내부 구조)
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class StoreSearchResponseDTO(
    @SerializedName("relatedUniversities") val relatedUniversities: List<String>?,
    @SerializedName("stores") val stores: List<StoreSearchItemDTO>?
)

// 2. 개별 가게 아이템 (stores 리스트 내부)
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class StoreSearchItemDTO(
    @SerializedName("storeId") val storeId: Long,
    @SerializedName("storeName") val storeName: String,
    @SerializedName("neighborhood") val neighborhood: String?,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("totalSeatCount") val totalSeatCount: Int,
    @SerializedName("availableSeatCount") val availableSeatCount: Int,
    @SerializedName("statusTag") val statusTag: String?, // 예: "CROWDED"
    @SerializedName("statusTagName") val statusTagName: String?, // 예: "혼잡"
    @SerializedName("images") val images: List<String>?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("distance") val distance: String?, // 예: "300m"
    @SerializedName("operationStatus") val operationStatus: String?,
    @SerializedName("storePhone") val storePhone: String?
)