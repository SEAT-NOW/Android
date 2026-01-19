package com.gmg.seatnow.presentation.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.theme.PointRed
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import com.gmg.seatnow.presentation.theme.White
import com.gmg.seatnow.presentation.user.home.UserHomeScreen
import com.gmg.seatnow.presentation.user.seatsearch.SeatSearchScreen

@Composable
fun UserMainScreen() {
    var currentTab by remember { mutableStateOf(UserTab.HOME) }

    // [핵심] 홈 탭으로 전달할 필터 데이터 (null이면 필터 없음)
    var searchFilterHeadCount by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        containerColor = White,
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = {
            UserBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    // 탭 변경 시 단순 이동만 처리
                    currentTab = tab
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(if (currentTab == UserTab.HOME) 1f else -1f)
                    .graphicsLayer { alpha = if (currentTab == UserTab.HOME) 1f else 0f }
            ) {
                UserHomeScreen(
                    initialHeadCount = searchFilterHeadCount,
                    onFilterCleared = { searchFilterHeadCount = null }
                )
            }

            // [2] 자리 찾기 (필터 입력) 화면
            if (currentTab == UserTab.SEAT_SEARCH) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(2f)
                        .background(White)
                ) {
                    SeatSearchScreen(
                        onSearchConfirmed = { headCount ->
                            // 1. 필터 값 설정
                            searchFilterHeadCount = headCount
                            // 2. 홈 탭으로 이동 (지도가 보이도록)
                            currentTab = UserTab.HOME
                        }
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