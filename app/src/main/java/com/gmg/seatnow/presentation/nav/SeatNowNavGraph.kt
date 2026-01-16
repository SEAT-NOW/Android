package com.gmg.seatnow.presentation.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gmg.seatnow.presentation.login.LoginScreen
import com.gmg.seatnow.presentation.owner.login.OwnerLoginScreen
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpScreen
import com.gmg.seatnow.presentation.owner.store.StoreMainRoute
import com.gmg.seatnow.presentation.owner.store.mypage.AccountInfoScreen
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageAction
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageViewModel
import com.gmg.seatnow.presentation.owner.store.withdraw.OwnerWithdrawScreen
import com.gmg.seatnow.presentation.splash.SplashScreen
import com.gmg.seatnow.presentation.user.UserMainScreen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SeatNowNavGraph(
    // [변경 1] AuthManager 파라미터 삭제 (UI는 데이터 관리를 몰라야 함)
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
                // ★ [수정] 사장님 메인 화면으로 이동하도록 연결
                onNavigateToOwnerMain = {
                    navController.navigate("store_main") { // "store_main"이 사장님 메인 라우트
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
            UserMainScreen()
        }

        // 4. 사장님 로그인
        composable("owner_login") {
            OwnerLoginScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToOwnerMain = {
                    // [변경 2] 토큰 저장 로직 삭제!
                    // (이미 ViewModel -> Repository에서 진짜 토큰을 저장했음)
                    // 여기서는 순수하게 화면 이동만 수행
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
            // StoreMainViewModel에서 로그아웃 처리를 하고 이벤트를 보내주면 더 좋음.
            // 여기서는 콜백으로 처리한다고 가정
            StoreMainRoute(
                onNavigateToLogin = {
                    // [변경 3] 토큰 삭제 로직 삭제 (Repository가 수행함)
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
                        // [변경 4] 여기도 토큰 삭제 삭제
                        navController.navigate("login") {
                            popUpTo("store_main") { inclusive = true }
                        }
                    }
                }
            }

            AccountInfoScreen(
                onBackClick = { navController.popBackStack() },
                onLogoutClick = { viewModel.onAction(MyPageAction.OnLogoutClick) }, // VM이 Repo.logout 호출
                onNavigateToWithdraw = { navController.navigate("owner_withdraw") }
            )
        }

        composable("owner_withdraw") {
            OwnerWithdrawScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToLogin = {
                    // [변경 5] 토큰 삭제 삭제
                    navController.navigate("login") {
                        popUpTo("store_main") { inclusive = true }
                    }
                }
            )
        }
    }
}