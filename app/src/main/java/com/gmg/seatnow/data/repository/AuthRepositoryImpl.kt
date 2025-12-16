package com.gmg.seatnow.data.repository

import android.content.Context
import android.util.Log
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
}