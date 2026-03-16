// 경로: com.gmg.seatnow.data.api.TokenAuthenticator.kt
package com.gmg.seatnow.data.api

import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.domain.repository.AuthRepository
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    private val authManager: AuthManager,
    private val authRepositoryProvider: Provider<AuthRepository>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // 1. 무한 루프 방지 (아래 추가한 헬퍼 함수 사용)
        if (responseCount(response) >= 3) {
            return null
        }

        val currentToken = authManager.getAccessToken()

        synchronized(this) {
            val updatedToken = authManager.getAccessToken()

            if (currentToken != updatedToken && updatedToken != null) {
                return response.request.newBuilder()
                    .header("Authorization", updatedToken)
                    .build()
            }

            val newAccessToken = authRepositoryProvider.get().refreshTokenBlocking()

            return if (newAccessToken != null) {
                response.request.newBuilder()
                    .header("Authorization", newAccessToken)
                    .build()
            } else {
                null
            }
        }
    }

    // [추가된 부분] OkHttp 응답이 몇 번 실패했는지 세어주는 헬퍼 함수
    private fun responseCount(response: Response?): Int {
        var result = 1
        var priorResponse = response?.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }
}