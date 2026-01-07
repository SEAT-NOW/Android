package com.gmg.seatnow.presentation.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gmg.seatnow.data.local.MockAuthManager
import com.gmg.seatnow.presentation.login.LoginScreen
import com.gmg.seatnow.presentation.owner.login.OwnerLoginScreen
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpScreen
import com.gmg.seatnow.presentation.owner.store.StoreMainRoute
import com.gmg.seatnow.presentation.owner.store.mypage.AccountInfoScreen
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageAction
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageViewModel
import com.gmg.seatnow.presentation.owner.store.withdraw.OwnerWithdrawScreen
import com.gmg.seatnow.presentation.splash.SplashScreen
import com.gmg.seatnow.presentation.user.home.UserMainScreen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SeatNowNavGraph(
    mockAuthManager: MockAuthManager,
    startDestination: String
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

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

        // 3. 사용자 메인 (지도 화면)
        composable("user_main") {
            // ★ [수정됨] 임시 텍스트 제거하고 실제 화면 연결
            UserMainScreen()
        }

        // 4. 사장님 로그인
        composable("owner_login") {
            OwnerLoginScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToOwnerMain = {
                    // 로그인 성공: 토큰 저장 -> 메인 이동
                    val fakeToken = mockAuthManager.generateMockToken()
                    mockAuthManager.saveToken(fakeToken)

                    navController.navigate("store_main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate("owner_signup")
                }
            )
        }

        // 5. 사장님 회원가입
        composable("owner_signup") {
            OwnerSignUpScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.popBackStack()
                }
            )
        }

        // 6. 사장님 메인 (StoreMain)
        composable("store_main") {
            StoreMainRoute(
                onNavigateToLogin = {
                    // 로그아웃 시 토큰 삭제 및 이동
                    mockAuthManager.clearToken()
                    navController.navigate("login") {
                        popUpTo("store_main") { inclusive = true }
                    }
                },
                onNavigateToAccountInfo = {
                    navController.navigate("account_info")
                }
            )
        }

        // 7. 계정 정보 수정 (AccountInfo)
        composable("account_info") {
            val viewModel = hiltViewModel<MyPageViewModel>()

            LaunchedEffect(true) {
                viewModel.event.collectLatest { event ->
                    if (event is MyPageViewModel.MyPageEvent.NavigateToLogin) {
                        mockAuthManager.clearToken()
                        navController.navigate("login") {
                            popUpTo("store_main") { inclusive = true }
                        }
                    }
                }
            }

            AccountInfoScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = { viewModel.onAction(MyPageAction.OnLogoutClick) },
                onNavigateToWithdraw = { navController.navigate("owner_withdraw") }
            )
        }

        composable("owner_withdraw") {
            OwnerWithdrawScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToLogin = {
                    mockAuthManager.clearToken()
                    navController.navigate("login") {
                        popUpTo("store_main") { inclusive = true }
                    }
                }
            )
        }
    }
}