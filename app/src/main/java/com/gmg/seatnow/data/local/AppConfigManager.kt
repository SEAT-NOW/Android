package com.gmg.seatnow.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // 환경 설정은 별도의 prefs 파일로 관리하는 것이 안전합니다.
    private val prefs: SharedPreferences = context.getSharedPreferences("app_config_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_TESTER = "is_tester"
    }

    fun setTesterMode(isTester: Boolean) {
        prefs.edit().putBoolean(KEY_IS_TESTER, isTester).apply()
    }

    fun isTester(): Boolean {
        return prefs.getBoolean(KEY_IS_TESTER, false)
    }
}