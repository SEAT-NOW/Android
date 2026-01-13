package com.gmg.seatnow.data.repository

import android.content.Context
import android.util.Log
import com.gmg.seatnow.data.api.AuthService
import com.gmg.seatnow.data.model.request.BusinessVerificationConfirmRequestDTO
import com.gmg.seatnow.data.model.request.EmailVerificationConfirmRequestDTO
import com.gmg.seatnow.data.model.request.EmailVerificationRequestDTO
import com.gmg.seatnow.data.model.request.SmsVerificationConfirmRequestDTO
import com.gmg.seatnow.data.model.request.SmsVerificationRequestDTO
import com.gmg.seatnow.data.model.response.ErrorResponse
import com.gmg.seatnow.domain.model.StoreSearchResult
import com.gmg.seatnow.domain.repository.AuthRepository
import com.google.gson.Gson
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authService: AuthService
) : AuthRepository {

    override suspend fun loginKakao(): Result<String> = suspendCoroutine { continuation ->
        // 1. 카카오톡 앱으로 로그인 가능한지 확인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    // 카톡 로그인 실패 시 웹 로그인 시도 (Fallback)
                    UserApiClient.instance.loginWithKakaoAccount(context) { tokenWeb, errorWeb ->
                        if (errorWeb != null) continuation.resume(Result.failure(errorWeb))
                        else if (tokenWeb != null) continuation.resume(Result.success(tokenWeb.accessToken))
                    }
                } else if (token != null) {
                    continuation.resume(Result.success(token.accessToken))
                }
            }
        } else {
            // 2. 카카오톡 없으면 웹으로 로그인
            UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
                if (error != null) continuation.resume(Result.failure(error))
                else if (token != null) continuation.resume(Result.success(token.accessToken))
            }
        }
    }

    override suspend fun loginOwner(email: String, password: String): Result<Unit> {
        // API 호출 흉내 (1초 지연)
        delay(1000)

        Log.d("AuthRepo", "입력된 이메일: [$email], 비밀번호: [$password]")
        // Mocking 로직: 여기에 정의
        val safeEmail = email.trim()
        val safePassword = password.trim()

        return if (safeEmail == "test@gmail.com" && safePassword == "asdf!1234") {
            Log.d("AuthRepo", "로그인 성공!")
            Result.success(Unit)
        } else {
            Log.e("AuthRepo", "로그인 실패 - 불일치")
            Result.failure(Exception("아이디 또는 비밀번호를 확인해주세요."))
        }
    }

    // ★ [추가] 인증번호 전송 구현 (Mock)
    override suspend fun requestEmailAuthCode(email: String): Result<Unit> {
        return try {
            val request = EmailVerificationRequestDTO(email = email)
            val response = authService.sendEmailVerification(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMessage = response.body()?.message
                    ?: response.errorBody()?.string()
                    ?: "인증번호 발송에 실패했습니다."
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun requestPhoneAuthCode(phoneNumber: String): Result<Unit> {
        return try {
            val request = SmsVerificationRequestDTO(phoneNumber = phoneNumber)
            val response = authService.sendSmsVerification(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                // 실패 시 에러 메시지 파싱
                val errorMessage = response.body()?.message
                    ?: response.errorBody()?.string()
                    ?: "인증번호 발송에 실패했습니다."
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ★ [추가] 인증번호 검증 구현 (Mock)
    override suspend fun verifyEmailAuthCode(email: String, code: String): Result<Unit> {
        return try {
            val request = EmailVerificationConfirmRequestDTO(email, code)
            val response = authService.verifyEmailCode(request)

            if (response.isSuccessful && response.body()?.success == true) {
                // 200 OK
                Result.success(Unit)
            } else {
                // 400 Bad Request 등 에러 처리
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun verifyPhoneAuthCode(phoneNumber: String, code: String): Result<Unit> {
        return try {
            val request = SmsVerificationConfirmRequestDTO(phoneNumber, code)
            val response = authService.verifySmsCode(request)

            if (response.isSuccessful && response.body()?.success == true) {
                // 200 OK
                Result.success(Unit)
            } else {
                // 400 Bad Request 등 에러 처리
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // 사업자 번호 검증 Mock
    override suspend fun verifyBusinessNumber(businessNumber: String): Result<Unit> {
        return try {
            val request = BusinessVerificationConfirmRequestDTO(businessNumber)
            val response = authService.verifyBusinessNumber(request)

            if (response.isSuccessful && response.body()?.success == true) {
                // 200 OK
                Result.success(Unit)
            } else {
                // 400 Bad Request 등 에러 처리
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // 상호명 검색 Mock
    override suspend fun searchStore(query: String): Result<List<StoreSearchResult>> {
        if (query.isBlank()) return Result.success(emptyList())

        return try {
            // API 호출 (기본 page=1, size=15 사용)
            val response = authService.searchPlaces(query = query)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data ?: emptyList()

                // DTO -> Domain Model 변환
                val mappedList = data.map { dto ->
                    StoreSearchResult(
                        placeName = dto.name,
                        addressName = dto.roadAddress,
                        latitude = dto.lat,
                        longitude = dto.lng
                    )
                }
                Result.success(mappedList)
            } else {
                // 에러 파싱
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // 주변 대학 찾기 Mock
    override suspend fun getNearbyUniversity(lat: Double, lng: Double): Result<List<String>> {
        return try {
            // API 호출
            val response = authService.getNearbyUniversities(lat = lat, lng = lng)

            if (response.isSuccessful && response.body()?.success == true) {
                // data가 null이면 빈 리스트 반환
                val univList = response.body()?.data ?: emptyList()
                Result.success(univList)
            } else {
                // 에러 파싱
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun ownerLogout(): Result<Unit> {
        delay(500)
        Log.d("AuthRepo", "로그아웃 성공")
        return Result.success(Unit)
    }

    override suspend fun ownerWithdraw(): Result<Unit> {
        delay(1000)
        Log.d("AuthRepo", "회원탈퇴 성공")
        return Result.success(Unit)
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            errorResponse.message ?: errorResponse.detail ?: "알 수 없는 오류가 발생했습니다."
        } catch (e: Exception) {
            "서버 통신 오류가 발생했습니다."
        }
    }
}