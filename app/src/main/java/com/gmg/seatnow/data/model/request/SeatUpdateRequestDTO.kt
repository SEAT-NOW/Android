package com.gmg.seatnow.data.model.request

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

// 전체 요청 껍데기
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class SeatUpdateRequestDTO(
    @SerializedName("storeId") val storeId: Long,
    @SerializedName("spaceUpdates") val spaceUpdates: List<SpaceUpdateDTO>
)

// 층(Space) 정보
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class SpaceUpdateDTO(
    @SerializedName("spaceId") val spaceId: Long,
    @SerializedName("tableUpdates") val tableUpdates: List<TableUpdateDTO>
)

// 테이블(Table) 정보 - usedCount 기준 통일
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class TableUpdateDTO(
    @SerializedName("tableConfigId") val tableConfigId: Long,
    @SerializedName("usedCount") val usedCount: Int
)