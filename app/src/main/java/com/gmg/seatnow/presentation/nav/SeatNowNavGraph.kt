package com.gmg.seatnow.presentation.nav

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import com.gmg.seatnow.presentation.owner.store.mypage.account.AccountInfoScreen
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageViewModel
import com.gmg.seatnow.presentation.owner.store.withdraw.OwnerWithdrawScreen
import com.gmg.seatnow.presentation.splash.SplashScreen
import com.gmg.seatnow.presentation.user.UserMainScreen
import com.gmg.seatnow.presentation.user.term.UserTermsScreen
import com.gmg.seatnow.presentation.user.term.UserTermsViewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.gmg.seatnow.presentation.login.DeveloperLoginScreen
import com.gmg.seatnow.presentation.owner.store.mypage.account.ChangePasswordScreen
import com.gmg.seatnow.presentation.owner.store.mypage.account.CheckPasswordScreen
import com.gmg.seatnow.presentation.owner.store.mypage.account.EditSeatConfigScreen
import com.gmg.seatnow.presentation.owner.store.mypage.store.EditStoreContactScreen
import com.gmg.seatnow.presentation.owner.store.storeManage.storeInfo.EditStoreInfoScreen
import com.gmg.seatnow.presentation.owner.store.storeManage.storeManageEdit.StoreEditMainScreen

// ★ [수정됨] Contract 파일로 분리된 Event와 Action을 최상위 패키지에서 Import
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageAction
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageEvent

import com.gmg.seatnow.presentation.user.detail.StoreDetailRoute
import com.gmg.seatnow.presentation.user.mypage.UserAccountInfoScreen
import com.gmg.seatnow.presentation.user.mypage.UserMyPageAction
import com.gmg.seatnow.presentation.user.mypage.UserMyPageEvent
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
                },
                onNavigateToDeveloperLogin = {
                    navController.navigate("developer_login")
                }
            )
        }

        composable("developer_login") {
            DeveloperLoginScreen(
                onNavigateToBack = { navController.popBackStack() },
                onNavigateToUserMain = {
                    navController.navigate("user_main") {
                        popUpTo("login") { inclusive = true }
                    }
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
                        is UserMyPageEvent.NavigateToLogin -> {
                            navController.navigate("login") {
                                popUpTo("user_main") { inclusive = true }
                            }
                        }
                        is UserMyPageEvent.NavigateToWithdraw -> {
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
            arguments = listOf(navArgument("storeId") { type = NavType.LongType }),
            deepLinks = listOf(navDeepLink { uriPattern = "seatnow://seatnow.r-e.kr/store/{storeId}" }),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            StoreDetailRoute(
                onBackClick = { navController.popBackStack() }
            )
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
                },
                onNavigateToEditAccount = {
                    navController.navigate("edit_account_info")
                },
                onNavigateToEditSeatConfig = {
                    navController.navigate("edit_seat_config")
                },
                onNavigateToEditStoreInfo = {
                    navController.navigate("edit_store_info")
                },
                onNavigateToEditStoreManagement = {
                    navController.navigate("store_edit_main")
                }
            )
        }

        // 7. 계정 정보 수정 (AccountInfo)
        composable("account_info") {
            val viewModel = hiltViewModel<MyPageViewModel>()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(true) {
                viewModel.event.collectLatest { event ->
                    // ★ [수정됨] MyPageViewModel.MyPageEvent 체이닝 제거
                    when(event) {
                        is MyPageEvent.NavigateToLogin -> {
                            navController.navigate("login") { popUpTo("store_main") { inclusive = true } }
                        }
                        is MyPageEvent.NavigateToCheckPassword -> {
                            navController.navigate("check_password")
                        }
                        else -> {}
                    }
                }
            }

            AccountInfoScreen(
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
                onLogoutClick = { viewModel.onAction(MyPageAction.OnLogoutClick) },
                onNavigateToWithdraw = { navController.navigate("owner_withdraw") },
                onCheckPasswordClick = { viewModel.onAction(MyPageAction.OnCheckPasswordClick) }
            )
        }

        // 7-1 계정 정보 수정-패스워드 변경 전 체크화면
        composable("check_password") {
            val viewModel = hiltViewModel<MyPageViewModel>()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(true) {
                viewModel.event.collectLatest { event ->
                    // ★ [수정됨]
                    if (event is MyPageEvent.NavigateToChangePassword) {
                        navController.navigate("change_password")
                    }
                }
            }

            CheckPasswordScreen(
                uiState = uiState,
                onAction = viewModel::onAction,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 7-2 계정 정보 수정-패스워드 변경 화면
        composable("change_password") {
            val viewModel = hiltViewModel<MyPageViewModel>()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(true) {
                viewModel.event.collectLatest { event ->
                    // ★ [수정됨]
                    when (event) {
                        is MyPageEvent.NavigateBack -> {
                            navController.popBackStack("account_info", inclusive = false)
                        }
                        is MyPageEvent.ShowToast -> {
                            // Toast 메시지 처리
                        }
                        else -> {}
                    }
                }
            }

            ChangePasswordScreen(
                uiState = uiState,
                onAction = viewModel::onAction,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 7-3 계정 정보 - 회원탈퇴 화면
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

        // 8. 가게 정보 구성 수정 화면
        composable("edit_store_info") {
            val viewModel = hiltViewModel<MyPageViewModel>()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(true) {
                viewModel.event.collectLatest { event ->
                    // ★ [수정됨]
                    if (event is MyPageEvent.NavigateToEditStoreContact) {
                        navController.navigate("edit_store_contact")
                    }
                }
            }

            EditStoreInfoScreen(
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
                onEditContactClick = { viewModel.onAction(MyPageAction.OnStoreContactClick) }
            )
        }

        // 8-1. 가게 연락처 수정 화면
        composable("edit_store_contact") {
            val viewModel = hiltViewModel<MyPageViewModel>()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(true) {
                viewModel.event.collectLatest { event ->
                    // ★ [수정됨]
                    if (event is MyPageEvent.NavigateBack) {
                        navController.popBackStack()
                    }
                }
            }

            EditStoreContactScreen(
                uiState = uiState,
                onAction = viewModel::onAction,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 9. 좌석 정보 구성 수정 화면
        composable("edit_seat_config") {
            val viewModel = hiltViewModel<MyPageViewModel>()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(true) {
                viewModel.event.collectLatest { event ->
                    // ★ [수정됨]
                    when (event) {
                        is MyPageEvent.NavigateBack -> {
                            navController.popBackStack()
                        }
                        is MyPageEvent.ShowToast -> {
                            // Toast 처리
                        }
                        else -> {}
                    }
                }
            }

            EditSeatConfigScreen(
                uiState = uiState,
                onAction = viewModel::onAction,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("store_edit_main") {
            StoreEditMainScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}