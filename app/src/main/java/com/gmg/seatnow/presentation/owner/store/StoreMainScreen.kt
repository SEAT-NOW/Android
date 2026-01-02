package com.gmg.seatnow.presentation.owner.store

import android.R.id.bold
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.component.SeatNowMenuItem
import kotlinx.coroutines.flow.collectLatest

// ViewModel 관련 import
import com.gmg.seatnow.presentation.owner.store.StoreMainViewModel.StoreMainEvent
import com.gmg.seatnow.presentation.owner.store.StoreMainViewModel.StoreMainUiState
import com.gmg.seatnow.presentation.owner.store.StoreMainAction
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.White

// StoreMainRoute는 기존 유지 (변경 없음)
@Composable
fun StoreMainRoute(
    viewModel: StoreMainViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToAccountInfo: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(true) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is StoreMainEvent.NavigateToLogin -> onNavigateToLogin()
                is StoreMainEvent.NavigateToAccountInfo -> onNavigateToAccountInfo()
            }
        }
    }

    StoreMainScreen(
        uiState = uiState,
        onAction = viewModel::onAction
    )
}

// StoreMainScreen도 기존 유지 (내용물인 MyPageContent만 수정됨)
@Composable
fun StoreMainScreen(
    uiState: StoreMainUiState,
    onAction: (StoreMainAction) -> Unit
) {
    Scaffold(
        containerColor = White,
        bottomBar = {
            NavigationBar(containerColor = White) {
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
                                color = if(isSelected) PointRed else SubGray
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = tab.iconResId),
                                contentDescription = null,
                                tint = if(isSelected) PointRed else SubGray
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("좌석 관리 화면")
                    }
                }
                StoreTab.MY_PAGE -> MyPageContent(onAction) // 여기가 핵심 수정
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

// 👇 여기 수정되었습니다. (미구현 기능 주석 처리)
@Composable
fun MyPageContent(onAction: (StoreMainAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            "마이페이지",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // 1. 계정 섹션 (유지)
        SectionHeader("계정")

        SeatNowMenuItem(
            text = "계정 정보 수정",
            onClick = { onAction(StoreMainAction.NavigateToAccountInfo) }
        )

//        HorizontalDivider(color = SubGray, thickness = 1.dp)

        Spacer(modifier = Modifier.height(24.dp))

        /* // 2. 운영 설정 (미구현 기능 주석 처리)
        SectionHeader("운영 설정")
        MenuItem(text = "가게 정보 수정") { }
        MenuItem(text = "좌석 구성 정보 수정") { }
        Divider(color = Color(0xFFEEEEEE))

        Spacer(modifier = Modifier.height(24.dp))

        // 3. 서비스 이용 (미구현 기능 주석 처리)
        SectionHeader("서비스 이용")
        MenuItem(text = "푸시 알림") { }
        */
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text,
        fontSize = 13.sp,
        color = SubGray,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun StoreMainScreenPreview() {
    StoreMainScreen(
        uiState = StoreMainUiState(
            currentTab = StoreTab.MY_PAGE,
            isLoading = false
        ),
        onAction = {}
    )
}