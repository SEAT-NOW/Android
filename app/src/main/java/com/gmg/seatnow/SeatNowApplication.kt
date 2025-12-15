package com.gmg.seatnow // 👈 패키지명 확인

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.kakao.sdk.common.KakaoSdk

@HiltAndroidApp
class SeatNowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Kakao SDK 초기화 (네이티브 앱 키 필요)
        KakaoSdk.init(this, "54be1fae0136e1d12dc327aae184d6ce")
    }
}