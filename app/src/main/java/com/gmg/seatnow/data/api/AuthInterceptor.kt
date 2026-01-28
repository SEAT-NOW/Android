package com.gmg.seatnow.data.api

import com.gmg.seatnow.data.local.AuthManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Provider

class AuthInterceptor @Inject constructor(
    private val authManager: AuthManager,
    // AuthService를 Provider로 감싸서 주입합니다. (순환 참조 방지)
    private val authServiceProvider: Provider<AuthService>
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = authManager.getAccessToken()

        // 1. 기존 토큰으로 요청 진행
        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNullOrBlank()) {
            val finalToken = if (token.startsWith("Bearer")) token else "Bearer $token"
            requestBuilder.addHeader("Authorization", finalToken)
        }

        var response = chain.proceed(requestBuilder.build())

        // 2. 토큰 만료(401 에러) 시 재발급 로직 실행
        if (response.code == 401) {
            val refreshToken = authManager.getRefreshToken()

            if (!refreshToken.isNullOrBlank()) {
                // synchronized: 여러 API가 동시에 401을 맞았을 때, 재발급 API가 여러 번 호출되는 것 방지
                synchronized(this) {
                    val currentToken = authManager.getAccessToken()
                    // 다른 쓰레드에서 이미 토큰을 갱신했다면, 그 토큰으로 바로 재시도
                    if (currentToken != token) {
                        response.close()
                        val newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer $currentToken")
                            .build()
                        return chain.proceed(newRequest)
                    }

                    // 토큰 재발급 API 동기 호출 (runBlocking 사용)
                    val authService = authServiceProvider.get()
                    val refreshResponse = runBlocking {
                        try {
                            authService.reissueToken(refreshToken)
                        } catch (e: Exception) {
                            null
                        }
                    }

                    // 3. 재발급 성공 시
                    if (refreshResponse != null && refreshResponse.isSuccessful && refreshResponse.body()?.success == true) {
                        val newTokens = refreshResponse.body()?.data
                        newTokens?.let {
                            // 새 토큰 저장
                            authManager.saveTokens(it.accessToken, it.refreshToken)

                            // 실패했던 원래 API를 새 토큰으로 재시도
                            response.close()
                            val newRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer ${it.accessToken}")
                                .build()
                            response = chain.proceed(newRequest)
                        }
                    } else {
                        // 4. 재발급 실패(리프레시 토큰까지 만료됨) -> 토큰 삭제하여 로그아웃 유도
                        authManager.clearTokens()
                    }
                }
            } else {
                authManager.clearTokens()
            }
        }

        return response
    }
}