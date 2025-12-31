package com.gmg.seatnow.presentation.nav

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gmg.seatnow.presentation.login.LoginScreen
import com.gmg.seatnow.presentation.owner.login.OwnerLoginScreen // Import 확인
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpScreen
import com.gmg.seatnow.presentation.splash.SplashScreen

@Composable
fun SeatNowNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {

        // 1. 스플래시 화면
        composable("splash") {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToUserMain = {
                    navController.navigate("user_main") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // 2. 로그인 화면 (일반 사용자/사장님 선택)
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

        // 3. 사용자 메인 (임시)
        composable("user_main") {
            androidx.compose.material3.Text("사용자 메인 화면 (지도)")
        }

        // 4. 사장님 로그인 (실제 연결)
        composable("owner_login") {
            OwnerLoginScreen(
                onBackClick = { navController.popBackStack() }, // 뒤로가기
                onNavigateToOwnerMain = {
                    // 로그인 성공 시 이동할 사장님 메인 화면 (임시 경로)
                    // 추후 owner_main 등의 경로로 변경 필요
                    navController.navigate("user_main") { // 일단 user_main으로 연결해둠
                        popUpTo("owner_login") { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    // 회원가입 화면으로 이동 (아직 미구현이므로 임시 처리)
                    navController.navigate("owner_signup")
                }
            )
        }

        // 5. 사장님 회원가입 첫번째 탭
//        composable(
//            route = "owner_signup",
//            enterTransition = {
//                slideIntoContainer(
//                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
//                    animationSpec = tween(400)
//                )
//            }
//        ) {
        composable("owner_signup") {
            OwnerSignUpScreen(
                onBackClick = { navController.popBackStack() }
            ) { }
        }

        composable("owner_signup") {
            OwnerSignUpScreen(
                onBackClick = { navController.popBackStack() },
                // ★ [수정] 완료(로그인 버튼) 시 'owner_login' 화면으로 복귀
                onNavigateToHome = {
                    // 회원가입 화면을 스택에서 제거하여 로그인 화면으로 돌아감
                    navController.popBackStack()
                }
            )
        }
    }
}