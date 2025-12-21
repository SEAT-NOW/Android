package com.gmg.seatnow.domain.repository

import com.gmg.seatnow.domain.model.StoreSearchResult

interface AuthRepository {
    // LoginScreen
    suspend fun loginKakao(): Result<String>

    // OwnerLoginScreen
    suspend fun loginOwner(email: String, password: String) : Result<Unit>

    // OwnerSignUpScreen_Step1
    suspend fun requestAuthCode(target: String): Result<Unit>
    suspend fun verifyAuthCode(target: String, code: String): Result<Unit>

    // OwnerSignUpScreen_Step2
    suspend fun verifyBusinessNumber(number: String): Result<Unit>
    suspend fun searchStore(query: String): Result<List<StoreSearchResult>> // 상호명 검색
    suspend fun getNearbyUniversity(lat: Double, lng: Double): Result<String> // 주소 기반 대학 찾기
}