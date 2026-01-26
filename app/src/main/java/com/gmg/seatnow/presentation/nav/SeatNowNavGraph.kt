package com.gmg.seatnow.presentation.nav

import StoreDetailRoute
import StoreDetailScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.gmg.seatnow.presentation.user.term.UserTermsScreen
import com.gmg.seatnow.presentation.user.term.UserTermsViewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.gmg.seatnow.presentation.user.mypage.UserAccountInfoScreen
import com.gmg.seatnow.presentation.user.mypage.UserMyPageAction
import com.gmg.seatnow.presentation.user.mypage.UserMyPageViewModel
import com.gmg.seatnow.presentation.user.mypage.UserWithdrawScreen

@Composable
fun SeatNowNavGraph(
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
                },
                onNavigateToOwnerMain = {
                    navController.navigate("store_main") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToTerms = { isGuest ->
                    navController.navigate("user_terms/$isGuest") {
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
                },
                onNavigateToTerms = { isGuest ->
                    navController.navigate("user_terms/$isGuest")
                }
            )
        }

        // 2-1 사용자 약관 동의 화면
        composable(
            route = "user_terms/{isGuest}",
            arguments = listOf(navArgument("isGuest") { type = NavType.BoolType })
        ) { backStackEntry ->
            val isGuest = backStackEntry.arguments?.getBoolean("isGuest") ?: false
            val viewModel = hiltViewModel<UserTermsViewModel>()

            UserTermsScreen(
                onNavigateToBack = { navController.popBackStack() },
                onNavigateToMain = {
                    viewModel.saveTermsAgreement(isGuest)
                    navController.navigate("user_main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // 3. 사용자 메인 (지도 화면)
        composable("user_main") {
            UserMainScreen(
                onNavigateToAccountInfo = {
                    navController.navigate("user_account_info")
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("user_main") { inclusive = true }
                    }
                },
                onNavigateToDetail = { storeId ->
                    navController.navigate("store_detail/$storeId")
                }
            )
        }

        // 3-1. 유저 계정 정보 화면
        composable("user_account_info") {
            val viewModel = hiltViewModel<UserMyPageViewModel>()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(true) {
                viewModel.event.collectLatest { event ->
                    when (event) {
                        // [수정됨] 게스트와 일반 유저의 로그아웃이 모두 여기를 탑니다.
                        // 메인화면(user_main)까지 백스택을 전부 비우고 로그인 화면으로 이동합니다.
                        is UserMyPageViewModel.UserMyPageEvent.NavigateToLogin -> {
                            navController.navigate("login") {
                                popUpTo("user_main") { inclusive = true }
                            }
                        }
                        is UserMyPageViewModel.UserMyPageEvent.NavigateToWithdraw -> {
                            navController.navigate("user_withdraw")
                        }
                        else -> {}
                    }
                }
            }

            UserAccountInfoScreen(
                nickname = uiState.nickname,
                onBackClick = { navController.popBackStack() },
                isGuest = uiState.isGuest,
                onLogoutClick = { viewModel.onAction(UserMyPageAction.OnLogoutClick) },
                onNavigateToWithdraw = { viewModel.onAction(UserMyPageAction.OnWithdrawClick) }
            )
        }

        // 3-2. 유저 회원 탈퇴 화면
        composable("user_withdraw") {
            UserWithdrawScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("user_main") { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 3-3 가게 상세 화면
        composable(
            route = "store_detail/{storeId}",
            arguments = listOf(navArgument("storeId") { type = NavType.LongType })
        ) {
            // ★ 뷰모델이 NavGraph의 storeId를 자동으로 가져가므로, 그냥 Route만 호출하면 됩니다.
            StoreDetailRoute()
        }

        // 4. 사장님 로그인
        composable("owner_login") {
            OwnerLoginScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToOwnerMain = {
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
                    navController.navigate("login") {
                        popUpTo("store_main") { inclusive = true }
                    }
                }
            )
        }
    }
}