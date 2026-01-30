package com.gmg.seatnow.presentation.owner.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.owner.store.manage.StoreManagementScreen
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
    onNavigateToAccountInfo: () -> Unit,
    onNavigateToEditAccount: () -> Unit,
    onNavigateToEditSeatConfig: () -> Unit,
    onNavigateToEditStoreInfo: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // StoreMainViewModel은 이제 탭 상태만 관리하므로 Event 수집 로직 불필요

    StoreMainScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToAccountInfo = onNavigateToAccountInfo,
        onNavigateToEditAccount = onNavigateToEditAccount,
        onNavigateToEditSeatConfig = onNavigateToEditSeatConfig,
        onNavigateToEditStoreInfo = onNavigateToEditStoreInfo
    )
}

@Composable
fun StoreMainScreen(
    uiState: StoreMainViewModel.StoreMainUiState,
    onAction: (StoreMainAction) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAccountInfo: () -> Unit,
    onNavigateToEditAccount: () -> Unit,
    onNavigateToEditSeatConfig: () -> Unit,
    onNavigateToEditStoreInfo: () -> Unit
) {
    Scaffold(
        containerColor = White,
        bottomBar = {
            StoreBottomNavigation(
                currentTab = uiState.currentTab,
                onTabSelected = { tab -> onAction(StoreMainAction.ChangeTab(tab)) }
            )
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
                StoreTab.STORE_MANAGEMENT -> {
                    StoreManagementScreen(
                        onEditStoreInfoClick = onNavigateToEditStoreInfo
                    )
                }
                StoreTab.MY_PAGE -> {
                    // ✅ 여기서 MyPageScreen을 호출하며 콜백을 전달합니다.
                    MyPageScreen(
                        onNavigateToAccountInfo = onNavigateToAccountInfo,
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToEditAccount = onNavigateToEditAccount,
                        onNavigateToEditSeatConfig = onNavigateToEditSeatConfig,
                        onNavigateToEditStoreInfo = onNavigateToEditStoreInfo
                    )
                }
            }
        }
    }
}

@Composable
fun StoreBottomNavigation(
    currentTab: StoreTab,
    onTabSelected: (StoreTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = White,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StoreTab.values().forEach { tab ->
                val isSelected = currentTab == tab
                val contentColor = if (isSelected) PointRed else SubGray // 선택 안됨 색상: SubGray

                val interactionSource = remember { MutableInteractionSource() }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onTabSelected(tab) }
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 아이콘과 텍스트를 감싸는 Column에 Ripple 효과 적용
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.indication(
                            interactionSource = interactionSource,
                            indication = rememberRipple(
                                bounded = false,
                                radius = 30.dp,
                                color = PointRed
                            )
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = tab.iconResId),
                            contentDescription = tab.title,
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}