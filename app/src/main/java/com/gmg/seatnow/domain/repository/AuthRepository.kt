package com.gmg.seatnow.domain.repository

interface AuthRepository {
    // LoginScreen
    suspend fun loginKakao(): Result<String>

    // OwnerLoginScreen
    suspend fun loginOwner(email: String, password: String) : Result<Unit>

    // OwnerSignUpScreen
    suspend fun requestAuthCode(target: String): Result<Unit>
    suspend fun verifyAuthCode(target: String, code: String): Result<Unit>
}