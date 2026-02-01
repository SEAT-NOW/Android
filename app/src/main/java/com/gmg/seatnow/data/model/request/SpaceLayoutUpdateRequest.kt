package com.gmg.seatnow.data.model.request

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

// 최상위 리스트 아이템 (공간)
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class SpaceLayoutUpdateRequest(
    @SerializedName("id") val id: Long?, // 신규 추가면 null, 수정이면 ID
    @SerializedName("name") val name: String,
    @SerializedName("tables") val tables: List<TableLayoutUpdateRequest>
)

// 내부 테이블 정보
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class TableLayoutUpdateRequest(
    @SerializedName("tableConfigId") val tableConfigId: Long?, // 신규면 null
    @SerializedName("tableType") val tableType: Int,       // N인석 (capacity)
    @SerializedName("tableCount") val tableCount: Int       // M개 (count)
)