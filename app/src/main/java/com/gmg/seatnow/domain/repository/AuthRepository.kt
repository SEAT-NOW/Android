package com.gmg.seatnow.domain.repository

interface AuthRepository {
    // LoginScreen
    suspend fun loginKakao(): Result<String>

    // OwnerLoginScreen
    suspend fun loginOwner(email: String, password: String) : Result<Unit>
}