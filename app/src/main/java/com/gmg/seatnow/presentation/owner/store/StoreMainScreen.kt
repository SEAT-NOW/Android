package com.gmg.seatnow.presentation.owner.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageScreen
import com.gmg.seatnow.presentation.owner.store.seat.SeatManagementScreen
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.White

// StoreMainRoute (네비게이션 콜백 전달자)
@Composable
fun StoreMainRoute(
    viewModel: StoreMainViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToAccountInfo: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // StoreMainViewModel은 이제 탭 상태만 관리하므로 Event 수집 로직 불필요

    StoreMainScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToAccountInfo = onNavigateToAccountInfo
    )
}

@Composable
fun StoreMainScreen(
    uiState: StoreMainViewModel.StoreMainUiState,
    onAction: (StoreMainAction) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAccountInfo: () -> Unit
) {
    Scaffold(
        containerColor = White,
        bottomBar = {
            NavigationBar(
                containerColor = White,
                tonalElevation = 0.dp) {
                StoreTab.values().forEach { tab ->
                    val isSelected = uiState.currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { onAction(StoreMainAction.ChangeTab(tab)) },
                        label = {
                            Text(
                                tab.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PointRed else SubGray
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = tab.iconResId),
                                contentDescription = null,
                                tint = if (isSelected) PointRed else SubGray
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = White)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(White)
        ) {
            when (uiState.currentTab) {
                StoreTab.SEAT_MANAGEMENT -> {
                    SeatManagementScreen()
                }
                StoreTab.MY_PAGE -> {
                    // ✅ 여기서 MyPageScreen을 호출하며 콜백을 전달합니다.
                    MyPageScreen(
                        onNavigateToAccountInfo = onNavigateToAccountInfo,
                        onNavigateToLogin = onNavigateToLogin
                    )
                }
            }
        }
    }
}