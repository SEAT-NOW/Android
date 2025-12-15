package com.gmg.seatnow.presentation.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gmg.seatnow.presentation.login.LoginScreen
import com.gmg.seatnow.presentation.splash.SplashScreen

@Composable
fun SeatNowNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        
        // 1. 스플래시 화면
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // 2. 로그인 화면
        composable("login") {
            LoginScreen(
                onNavigateToUserMain = {
                    navController.navigate("user_main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToOwnerLogin = {
                    navController.navigate("owner_login")
                }
            )
        }

        // 3. 사용자 메인 (임시) - 나중에 user/map 패키지로 이동
        composable("user_main") {
            androidx.compose.material3.Text("사용자 메인 화면 (지도)")
        }

        // 4. 사장님 로그인 (임시) - 나중에 owner/auth 패키지로 이동
        composable("owner_login") {
            androidx.compose.material3.Text("사장님 로그인 화면")
        }
    }
}