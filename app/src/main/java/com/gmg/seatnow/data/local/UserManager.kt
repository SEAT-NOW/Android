package com.gmg.seatnow.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_NICKNAME = "user_nickname"
        private const val KEY_AGREED_GUEST = "is_agreed_guest"
        private const val KEY_AGREED_KAKAO = "is_agreed_kakao"
    }

    fun saveUserInfo(nickname: String?) {
        prefs.edit().putString(KEY_USER_NICKNAME, nickname).apply()
    }

    fun getUserNickname(): String? = prefs.getString(KEY_USER_NICKNAME, null)

    fun setGuestTermsAgreed(isAgreed: Boolean) {
        prefs.edit().putBoolean(KEY_AGREED_GUEST, isAgreed).apply()
    }

    fun isGuestTermsAgreed(): Boolean = prefs.getBoolean(KEY_AGREED_GUEST, false)

    fun setKakaoTermsAgreed(isAgreed: Boolean) {
        prefs.edit().putBoolean(KEY_AGREED_KAKAO, isAgreed).apply()
    }

    fun isKakaoTermsAgreed(): Boolean = prefs.getBoolean(KEY_AGREED_KAKAO, false)

    fun clearUserData() {
        // [수정] 로그아웃 시 약관 동의 내역은 유지하고 유저 닉네임(세션 정보)만 삭제합니다.
        prefs.edit().remove(KEY_USER_NICKNAME).apply()
    }

    // 완전히 탈퇴하거나 초기화할 때만 사용
    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}