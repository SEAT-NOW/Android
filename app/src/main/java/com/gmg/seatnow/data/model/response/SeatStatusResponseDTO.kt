package com.gmg.seatnow.data.model.response

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class SeatStatusResponseDTO(
    @SerialName("spaces") val spaces: List<SpaceStatusDTO>? = emptyList()
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class SpaceStatusDTO(
    @SerialName("spaceId") val spaceId: Long = 0L,

    // ★ [핵심 수정 1] 서버는 "spaceName"을 보냅니다. (기존 "name" 아님)
    @SerialName("spaceName") val name: String? = "",

    @SerialName("tables") val tables: List<TableStatusDTO>? = emptyList()
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class TableStatusDTO(
    // ★ [핵심 수정 2] 서버는 "tableConfigId"를 보냅니다. (기존 "tableId" 아님)
    @SerialName("tableConfigId") val tableId: Long = 0L,

    // ★ [핵심 수정 3] 서버는 "tableType"(좌석수)을 보냅니다. (기존 "seatCount" 아님)
    @SerialName("tableType") val seatCount: Int = 0,

    // ★ [핵심 수정 4] 서버는 "tableCount"(총개수)를 보냅니다.
    @SerialName("tableCount") val totalCount: Int = 0,

    // ★ [핵심 수정 5] 서버는 "usedCount"(사용중)를 보냅니다.
    @SerialName("usedCount") val usedCount: Int = 0
)