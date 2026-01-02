package com.gmg.seatnow.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * 토큰 저장 및 로그인 상태를 관리하는 Mocking Class
 * 실제 앱에서는 EncryptedSharedPreferences나 DataStore를 사용해야 합니다.
 */
class MockAuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mock_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
    }

    // 토큰 저장 (로그인 성공 시 호출)
    fun saveToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    // 토큰 삭제 (로그아웃/탈퇴 시 호출)
    fun clearToken() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply()
    }

    // 토큰 존재 여부 확인 (자동 로그인 판단용)
    fun hasToken(): Boolean {
        return !prefs.getString(KEY_ACCESS_TOKEN, null).isNullOrEmpty()
    }
    
    // 테스트용 가짜 토큰 발급
    fun generateMockToken(): String = "mock_token_${System.currentTimeMillis()}"
}