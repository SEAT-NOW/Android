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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.White
import com.gmg.seatnow.presentation.user.home.UserHomeScreen

@Composable
fun UserMainScreen() {
    var currentTab by remember { mutableStateOf(UserTab.HOME) }

    Scaffold(
        containerColor = White,
        bottomBar = {
            UserBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // [1] 지도 화면 (항상 렌더링)
            // zIndex: 홈 탭일 때 가장 위(1f), 아니면 뒤(-1f)로 보내 터치 차단
            // alpha: 홈 탭일 때 보임(1f), 아니면 투명(0f)
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

            // [2] 자리 찾기 화면 (탭이 선택되었을 때만 렌더링해도 무방, 가벼운 화면이므로)
            if (currentTab == UserTab.SEAT_SEARCH) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(2f) // 지도보다 위에 그려짐
                        .background(White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("N명 자리찾기 화면 준비중", color = SubGray)
                }
            }
        }
    }
}

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

                // 1. 클릭 상태를 감지하는 소스 생성 (부모-자식 공유용)
                val interactionSource = remember { MutableInteractionSource() }

                // 2. 부모: 터치 영역은 넓게 (weight 1f), 하지만 시각 효과는 끔 (indication = null)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = interactionSource, // 상태 공유
                            indication = null,                 // ★ 부모의 거대 Ripple 끄기
                            onClick = { onTabSelected(tab) }
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 3. 자식: 실제 Ripple이 그려질 위치 (Icon + Text 그룹)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.indication(
                            interactionSource = interactionSource, // ★ 부모가 클릭되면 여기서 반응함
                            indication = rememberRipple(
                                bounded = false,   // false = 동그랗게 퍼짐 / true = 네모나게 꽉 참
                                radius = 30.dp,    // ★ 물결 크기 조절 (이걸로 영역 조절하세요)
                                color = PointRed   // 물결 색상 (원하는 색으로 변경 가능)
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