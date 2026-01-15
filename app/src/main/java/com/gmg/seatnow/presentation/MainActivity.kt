package com.gmg.seatnow.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gmg.seatnow.data.local.AuthManager // Import 필수
import com.gmg.seatnow.presentation.nav.SeatNowNavGraph
import com.gmg.seatnow.presentation.theme.SeatNowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 1. 매니저 생성 (Hilt 주입 대신 간단하게 생성)
        val authManager = AuthManager(applicationContext)

        // 2. 시작 화면 결정 (토큰 있으면 바로 메인, 없으면 스플래시 or 로그인)
        // 스플래시가 있으니 일단 "splash"로 시작하되,
        // 스플래시 내부에서 mockAuthManager.hasToken()을 검사해서 분기하는 게 정석이지만,
        // 여기서는 간단하게 "이미 로그인 돼있으면 store_main"으로 설정
        val startDest = if (authManager.hasToken()) "store_main" else "splash"


        setContent {
            SeatNowTheme {
                // 3. NavGraph에 매니저와 시작점 전달
                SeatNowNavGraph(
                    authManager = authManager,
                    startDestination = startDest
                )
            }
        }
    }
}