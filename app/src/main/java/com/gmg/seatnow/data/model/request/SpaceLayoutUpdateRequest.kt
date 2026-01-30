package com.gmg.seatnow.data.model.request

// 최상위 리스트 아이템 (공간)
data class SpaceLayoutUpdateRequest(
    val id: Long?, // 신규 추가면 null, 수정이면 ID
    val name: String,
    val tables: List<TableLayoutUpdateRequest>
)

// 내부 테이블 정보
data class TableLayoutUpdateRequest(
    val tableConfigId: Long?, // 신규면 null
    val tableType: Int,       // N인석 (capacity)
    val tableCount: Int       // M개 (count)
)