package com.gmg.seatnow.data.api

import com.gmg.seatnow.data.local.AuthManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val authManager: AuthManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        // 1. 저장된 액세스 토큰 가져오기
        val token = authManager.getAccessToken()

        // 2. 토큰이 있다면 헤더에 추가 (Bearer 형식이 아니면 붙여줌)
        if (!token.isNullOrBlank()) {
            val finalToken = if (token.startsWith("Bearer")) token else "Bearer $token"
            builder.addHeader("Authorization", finalToken)
        }

        return chain.proceed(builder.build())
    }
}