package com.gmg.seatnow.domain.repository

interface AuthRepository {
    suspend fun loginKakao(): Result<String>
}