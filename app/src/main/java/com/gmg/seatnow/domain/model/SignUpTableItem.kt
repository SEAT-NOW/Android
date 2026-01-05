package com.gmg.seatnow.domain.model

data class SignUpTableItem(
    val id: Long = System.currentTimeMillis(),
    val personCount: String, // N인
    val tableCount: String   // M개
)