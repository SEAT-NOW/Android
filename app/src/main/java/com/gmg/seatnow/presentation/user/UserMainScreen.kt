package com.gmg.seatnow.presentation.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.White
import com.gmg.seatnow.presentation.user.home.UserHomeScreen
import com.gmg.seatnow.presentation.user.keep.KeepScreen
import com.gmg.seatnow.presentation.user.mypage.UserMyPageScreen
import com.gmg.seatnow.presentation.user.seatsearch.SeatSearchScreen

@Composable
fun UserMainScreen(
    // [수정] NavGraph에서 받아올 네비게이션 콜백 추가
    onNavigateToAccountInfo: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    var currentTab by rememberSaveable { mutableStateOf(UserTab.HOME) }
    var searchFilterHeadCount by rememberSaveable { mutableStateOf<Int?>(null) }

    Scaffold(
        containerColor = White,
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = {
            UserBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { tab -> currentTab = tab }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            // [1] 홈 화면
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(if (currentTab == UserTab.HOME) 1f else -1f)
                    .graphicsLayer { alpha = if (currentTab == UserTab.HOME) 1f else 0f }
            ) {
                UserHomeScreen(
                    initialHeadCount = searchFilterHeadCount,
                    onFilterCleared = { searchFilterHeadCount = null },
                    onNavigateToDetail = onNavigateToDetail
                )
            }

            // [2] 자리 찾기 화면
            if (currentTab == UserTab.SEAT_SEARCH) {
                Box(modifier = Modifier.fillMaxSize().zIndex(2f).background(White)) {
                    SeatSearchScreen(
                        onSearchConfirmed = { headCount ->
                            searchFilterHeadCount = headCount
                            currentTab = UserTab.HOME
                        }
                    )
                }
            }

            // [3] 킵 화면
            if (currentTab == UserTab.KEEP) {
                Box(modifier = Modifier.fillMaxSize().zIndex(2f).background(White)) {
                    KeepScreen(
                        onNavigateToDetail = onNavigateToDetail
                    )
                }
            }

            // [4] 마이페이지 화면
            if (currentTab == UserTab.MY_PAGE) {
                Box(modifier = Modifier.fillMaxSize().zIndex(2f).background(White)) {
                    // [수정] NavGraph에서 받아온 콜백을 그대로 전달
                    UserMyPageScreen(
                        onNavigateToAccountInfo = onNavigateToAccountInfo,
                        onNavigateToLogin = onNavigateToLogin
                    )
                }
            }
        }
    }
}

// UserBottomNavigation은 기존과 동일
@Composable
fun UserBottomNavigation(
    currentTab: UserTab,
    onTabSelected: (UserTab) -> Unit
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
                .height(64.dp),          // 높이는 여기서 지정
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserTab.values().forEach { tab ->
                val isSelected = currentTab == tab
                val contentColor = if (isSelected) PointRed else Color.DarkGray

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