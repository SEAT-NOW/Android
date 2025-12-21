package com.gmg.seatnow.presentation.owner.dataClass

data class SpaceItem(
    val id: Long = System.currentTimeMillis(), // 고유 ID
    val name: String,
    val seatCount: Int = 0, // 나중에 테이블 계산 로직 붙으면 자동 업데이트
    val isEditing: Boolean = false, // ★ 수정 모드인지 여부
    val editInput: String = "" // 수정 중일 때 임시 저장할 텍스트
)