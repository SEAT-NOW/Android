package com.gmg.seatnow.data.model.response

import com.google.gson.annotations.SerializedName

data class SeatStatusResponseDTO(
    @SerializedName("spaces") val spaces: List<SpaceStatusDTO>?
)

data class SpaceStatusDTO(
    @SerializedName("spaceId") val spaceId: Long,
    @SerializedName("spaceName") val name: String?,
    @SerializedName("tables") val tables: List<TableStatusDTO>?
)

data class TableStatusDTO(
    @SerializedName("tableConfigId") val tableId: Long,
    // "name" 필드는 서버 응답에 없습니다. 직접 생성해야 함.
    @SerializedName("tableType") val seatCount: Int,
    @SerializedName("tableCount") val totalCount: Int,
    @SerializedName("usedCount") val usedCount: Int
)