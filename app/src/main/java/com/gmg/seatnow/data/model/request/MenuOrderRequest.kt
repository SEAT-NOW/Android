package com.gmg.seatnow.data.model.request

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

// 전체 요청 Body
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class MenuOrderRequest(
    @SerializedName("categoryOrders") val categoryOrders: List<CategoryOrderDto>
)

// 내부 리스트 아이템
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class CategoryOrderDto(
    @SerializedName("categoryId") val categoryId: Long,
    @SerializedName("menuIds") val menuIds: List<Long>
)