package com.gmg.seatnow.data.api

import android.util.Log
import com.gmg.seatnow.data.local.AuthManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Provider

class AuthInterceptor @Inject constructor(
    private val authManager: AuthManager,
    private val authServiceProvider: Provider<AuthService>
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestUrl = originalRequest.url.toString()
        val accessToken = authManager.getAccessToken()

        val requestBuilder = originalRequest.newBuilder()

        // 1. [중요] "reissue" 요청이 아닐 때만 Access Token을 넣음
        // reissue 요청에 만료된 Access Token이 들어가면 서버 설정에 따라 401이 발생하여 무한 루프에 빠질 수 있음
        if (!requestUrl.contains("/auth/reissue") && !accessToken.isNullOrBlank()) {
            val finalToken = if (accessToken.startsWith("Bearer")) accessToken else "Bearer $accessToken"
            requestBuilder.addHeader("Authorization", finalToken)
        }

        var response = chain.proceed(requestBuilder.build())

        // 2. 401(토큰 만료) 발생 시 처리
        if (response.code == 401) {

            // ★ [방어 코드] 만약 401이 터진 요청이 이미 '재발급 요청'이었다면?
            // Refresh Token도 만료된 것이므로 즉시 로그아웃 처리 (무한 루프 방지)
            if (requestUrl.contains("/auth/reissue")) {
                Log.e("AuthInterceptor", "Refresh Token 만료됨. 로그아웃 진행")
                authManager.clearTokens()
                return response
            }

            val refreshToken = authManager.getRefreshToken()

            // Refresh Token이 있으면 재발급 시도
            if (!refreshToken.isNullOrBlank()) {
                synchronized(this) {
                    // 동기화 블록 진입 후 토큰이 그새 갱신되었는지 더블 체크
                    val currentToken = authManager.getAccessToken()
                    if (currentToken != null && currentToken != accessToken) {
                        response.close()
                        val newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer $currentToken")
                            .build()
                        return chain.proceed(newRequest)
                    }

                    // 재발급 API 호출
                    val authService = authServiceProvider.get()
                    val refreshResponse = runBlocking {
                        try {
                            // ★ 헤더에 RefreshToken만 담아서 보냄 (AuthService 설정 따름)
                            authService.reissueToken(refreshToken)
                        } catch (e: Exception) {
                            Log.e("AuthInterceptor", "재발급 통신 에러: ${e.message}")
                            null
                        }
                    }

                    if (refreshResponse != null && refreshResponse.isSuccessful && refreshResponse.body()?.success == true) {
                        val data = refreshResponse.body()?.data

                        if (data != null) {
                            // ★ [수정] 토큰 뿐만 아니라 storeId, userId도 갱신될 수 있으므로 같이 저장
                            // (스웨거 응답에 storeId가 포함되어 있으므로, 갱신해주는 것이 안전함)
                            // AuthManager에 updateTokens 메서드가 없다면 saveLoginData 등을 활용
                            authManager.saveLoginData(
                                accessToken = data.accessToken,
                                refreshToken = data.refreshToken,
                                storeId = data.storeId ?: authManager.getStoreId() // storeId가 오면 덮어쓰기
                            )

                            Log.d("AuthInterceptor", "토큰 재발급 성공")

                            // 원래 실패했던 요청을 새 토큰으로 재시도
                            response.close()
                            val newRequest = originalRequest.newBuilder()
                                .removeHeader("Authorization") // 기존 헤더 제거
                                .addHeader("Authorization", "Bearer ${data.accessToken}") // 새 토큰 추가
                                .build()
                            response = chain.proceed(newRequest)
                        } else {
                            // 데이터가 비어있음
                            authManager.clearTokens()
                        }
                    } else {
                        // 재발급 실패 (Refresh Token 만료 등) -> 로그아웃
                        Log.e("AuthInterceptor", "재발급 실패(4xx/5xx). 로그아웃")
                        authManager.clearTokens()
                    }
                }
            } else {
                // Refresh Token 없음 -> 로그아웃
                Log.e("AuthInterceptor", "Refresh Token 없음. 로그아웃")
                authManager.clearTokens()
            }
        }

        return response
    }
}