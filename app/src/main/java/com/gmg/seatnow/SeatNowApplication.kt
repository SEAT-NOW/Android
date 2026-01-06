package com.gmg.seatnow // 👈 패키지명 확인

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.kakao.sdk.common.KakaoSdk
import com.naver.maps.map.NaverMapSdk

@HiltAndroidApp
class SeatNowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Kakao SDK 초기화 (네이티브 앱 키 필요)
//        KakaoSdk.init(this, "54be1fae0136e1d12dc327aae184d6ce")
//        NaverMapSdk.getInstance(this).client =
//            NaverMapSdk.NaverCloudPlatformClient("C4AEg1ANRk14IuxBU6ae9gqxNRc5SBkJI6EfsYVS")

//        NaverMapSdk.getInstance(this).client =
//            NaverMapSdk.NaverCloudPlatformClient(BuildConfig.NAVER_CLIENT_ID)
//
//        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isNotEmpty()) {
//            KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
//        }
    }
}