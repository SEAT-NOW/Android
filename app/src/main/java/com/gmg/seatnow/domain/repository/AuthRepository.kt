package com.gmg.seatnow.domain.repository

import android.net.Uri
import com.gmg.seatnow.data.model.request.OwnerSignUpRequestDTO
import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.model.StoreSearchResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    // LoginScreen
    suspend fun loginKakao(): Result<String>

    // OwnerLoginScreen
    suspend fun loginOwner(email: String, password: String) : Result<Unit>

    // OwnerSignUpScreen_Step1
    suspend fun requestPhoneAuthCode(phoneNumber: String): Result<Unit>
    suspend fun requestEmailAuthCode(email: String): Result<Unit>
    suspend fun verifyPhoneAuthCode(phoneNumber: String, code: String): Result<Unit>
    suspend fun verifyEmailAuthCode(email: String, code: String): Result<Unit>

    // OwnerSignUpScreen_Step2
    suspend fun verifyBusinessNumber(number: String): Result<Unit>
    suspend fun searchStore(query: String): Result<List<StoreSearchResult>> // 상호명 검색
    suspend fun getNearbyUniversity(lat: Double, lng: Double): Result<List<String>> // 주소 기반 대학 찾기

    // OwnerSignUp
    suspend fun signUpOwner(
        requestDto: OwnerSignUpRequestDTO,
        licenseUri: Uri?,
        storeImageUris: List<Uri>
    ): Result<Unit>

    // OwnerStore
    suspend fun ownerLogout(): Result<Unit> // 로그아웃
    suspend fun ownerWithdraw(): Result<Unit> // 회원탈퇴

}