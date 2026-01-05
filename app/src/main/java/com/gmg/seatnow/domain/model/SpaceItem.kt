package com.gmg.seatnow.domain.model

data class SpaceItem(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val seatCount: Int = 0,
    val isEditing: Boolean = false,
    val editInput: String = "",
    val inputError: String? = null, // ★ 에러 메시지 개별 관리
    val tableList: List<SignUpTableItem> = listOf(SignUpTableItem(personCount="", tableCount=""))
)