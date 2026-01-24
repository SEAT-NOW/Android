package com.gmg.seatnow.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token" // [신규] Refresh Token 키
        private const val KEY_STORE_ID = "store_id" // API조회를 위한 store Id
        private const val KEY_AGREED_GUEST = "is_agreed_guest"
        private const val KEY_AGREED_KAKAO = "is_agreed_kakao"

        private const val KEY_USER_NICKNAME = "user_nickname"
    }

    fun saveUserInfo(nickname: String?) {
        prefs.edit()
            .putString(KEY_USER_NICKNAME, nickname)
            .apply()
    }

    // 토큰 저장 (로그인 성공 시 둘 다 저장)
    fun saveLoginData(accessToken: String, refreshToken: String, storeId: Long) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_STORE_ID, storeId)
            .apply()
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    // Access Token만 갱신 (재발급 시)
    fun saveAccessToken(accessToken: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, accessToken).apply()
    }

    fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_STORE_ID) // ★ 삭제
            .remove(KEY_USER_NICKNAME)
            .apply()
    }

    // Access Token 가져오기
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    // Refresh Token 가져오기
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    // Store Id 가져오기
    fun getStoreId(): Long {
        return prefs.getLong(KEY_STORE_ID, -1L)
    }

    // 토큰이 둘 다 있어야 로그인된 것으로 간주
    fun hasToken(): Boolean {
        return !getAccessToken().isNullOrEmpty() && !getRefreshToken().isNullOrEmpty()
    }

    fun setGuestTermsAgreed(isAgreed: Boolean) {
        prefs.edit().putBoolean(KEY_AGREED_GUEST, isAgreed).apply()
    }

    fun isGuestTermsAgreed(): Boolean {
        return prefs.getBoolean(KEY_AGREED_GUEST, false)
    }

    fun setKakaoTermsAgreed(isAgreed: Boolean) {
        prefs.edit().putBoolean(KEY_AGREED_KAKAO, isAgreed).apply()
    }

    fun isKakaoTermsAgreed(): Boolean {
        return prefs.getBoolean(KEY_AGREED_KAKAO, false)
    }

    fun getUserNickname(): String? = prefs.getString(KEY_USER_NICKNAME, null)
}