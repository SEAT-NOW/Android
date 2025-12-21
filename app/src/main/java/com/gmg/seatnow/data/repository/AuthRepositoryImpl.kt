package com.gmg.seatnow.data.repository

import android.content.Context
import android.util.Log
import com.gmg.seatnow.domain.model.StoreSearchResult
import com.gmg.seatnow.domain.repository.AuthRepository
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
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
    override suspend fun requestAuthCode(target: String): Result<Unit> {
        delay(1000) // API 호출 흉내
        Log.d("AuthRepo", "인증번호 요청: $target")

        return if (target.isNotBlank()) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("잘못된 입력입니다."))
        }
    }

    // ★ [추가] 인증번호 검증 구현 (Mock)
    override suspend fun verifyAuthCode(target: String, code: String): Result<Unit> {
        delay(500) // API 호출 흉내
        Log.d("AuthRepo", "인증번호 검증 시도: $target / $code")

        return if (code == "000000") {
            Result.success(Unit)
        } else {
            Result.failure(Exception("인증번호가 일치하지 않습니다."))
        }
    }

    // 사업자 번호 검증 Mock
    override suspend fun verifyBusinessNumber(number: String): Result<Unit> {
        delay(500)
        // 예시: 10자리이고 1로 끝나면 성공이라고 가정
        return if (number == "0000000000") Result.success(Unit)
        else Result.failure(Exception("유효하지 않은 사업자 번호입니다."))
    }

    // 상호명 검색 Mock
    override suspend fun searchStore(query: String): Result<List<StoreSearchResult>> {
        delay(300) // API 호출 흉내
        if (query.isBlank()) return Result.success(emptyList())

        // 더미 데이터 생성
        val mockList = listOf(
            StoreSearchResult("용용선생 대학로점", "서울 종로구 대학로8가길 36 1층", 37.582, 127.001),
            StoreSearchResult("용용선생 건대점", "서울 광진구 아차산로33길 25", 37.541, 127.067),
            StoreSearchResult("용용선생 강남역점", "서울 강남구 강남대로96길 17", 37.498, 127.027)
        )
        // 검색어가 포함된 것만 필터링하는 척 하거나 그냥 리턴
        return Result.success(mockList)
    }

    // 주변 대학 찾기 Mock
    override suspend fun getNearbyUniversity(lat: Double, lng: Double): Result<String> {
        delay(800)
        Log.d("AuthRepo", "대학 검색 요청: $lat, $lng")
        // 좌표에 따라 다른 대학 리턴 (Mock)
        return Result.success("명지대학교")
    }
}