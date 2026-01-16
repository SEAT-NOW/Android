package com.gmg.seatnow.domain.model

// 관리자가 실제로 다루게 될 핵심 데이터 모델 (Int 기반)
data class TableItem(
    val id: String,            // 테이블 고유 ID (DB PK)
    val floorId: String,       // ★ [추가] 층 ID (필터링용, 예: spaceId)
    val label: String,         // 예: "4인 테이블"
    val capacityPerTable: Int, // 테이블 당 인원 (4, 2...)
    val maxTableCount: Int,    // 전체 보유 개수
    val currentCount: Int = 0  // 현재 이용 중인 개수
)