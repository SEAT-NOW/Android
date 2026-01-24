package com.gmg.seatnow.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gmg.seatnow.data.local.AuthManager // Import 필수
import com.gmg.seatnow.presentation.nav.SeatNowNavGraph
import com.gmg.seatnow.presentation.theme.SeatNowTheme
import com.kakao.sdk.common.util.Utility
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val keyHash = Utility.getKeyHash(this)
//        Log.d("KakaoKeyHash", "현재 프로젝트의 키 해시: $keyHash")

//        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            SeatNowTheme {
                // 3. NavGraph에 매니저와 시작점 전달
                SeatNowNavGraph(
                    startDestination = "splash"
                )
            }
        }
    }
}