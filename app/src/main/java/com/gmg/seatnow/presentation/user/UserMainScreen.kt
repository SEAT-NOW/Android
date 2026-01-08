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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.White
import com.gmg.seatnow.presentation.user.home.UserHomeScreen
import com.gmg.seatnow.presentation.user.seatsearch.SeatSearchScreen

@Composable
fun UserMainScreen() {
    // [수정] 키보드 상태 감지 (MainActivity 설정이 되어야 정상 작동)
    val density = LocalDensity.current
    val ime = WindowInsets.ime
    val isKeyboardOpen by remember {
        derivedStateOf { ime.getBottom(density) > 0 }
    }

    var currentTab by remember { mutableStateOf(UserTab.HOME) }

    // [수정] mutableLongStateOf -> mutableStateOf (에러 방지)
    var seatSearchResetKey by remember { mutableStateOf(0L) }

    Scaffold(
        containerColor = White,
        bottomBar = {
            // 키보드가 열려있으면 바텀바를 아예 그리지 않음 (숨김)
            if (!isKeyboardOpen) {
                UserBottomNavigation(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        if (tab == UserTab.SEAT_SEARCH) {
                            seatSearchResetKey = System.currentTimeMillis()
                        }
                        currentTab = tab
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding) // 바텀바가 사라지면 padding.bottom은 0이 됨
                .fillMaxSize()
        ) {
            // [1] 지도 화면
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(if (currentTab == UserTab.HOME) 1f else -1f)
                    .graphicsLayer {
                        alpha = if (currentTab == UserTab.HOME) 1f else 0f
                    }
            ) {
                UserHomeScreen()
            }

            // [2] 자리 찾기 화면
            if (currentTab == UserTab.SEAT_SEARCH) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(2f)
                        .background(White)
                ) {
                    SeatSearchScreen(resetKey = seatSearchResetKey)
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
        modifier = Modifier.height(64.dp),
        color = White,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
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