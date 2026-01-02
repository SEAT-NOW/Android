package com.gmg.seatnow.presentation.owner.dataclass

data class TableItem(
    val id: Long = System.currentTimeMillis(),
    val personCount: String, // N인
    val tableCount: String   // M개
)