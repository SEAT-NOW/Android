package com.gmg.seatnow.presentation.user.mypage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.theme.SubBlack
import com.gmg.seatnow.presentation.theme.SubDarkGray
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAccountInfoScreen(
    nickname: String,
    isGuest: Boolean,      // [추가] 게스트 여부
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToWithdraw: () -> Unit
) {
    Scaffold(
        topBar = { SeatNowTopAppBar(title = "계정 정보 조회", onBackClick = onBackClick) },
        containerColor = White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 타이틀 ---------- 실제값 배치 (닉네임)
            UserInfoRow(title = "닉네임", value = nickname)

            Spacer(modifier = Modifier.height(70.dp)) // 하단 버튼을 바닥으로 밀어내기

            Column(
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                Text(
                    text = "로그아웃",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SubGray,
                    modifier = Modifier
                        .clickable(onClick = onLogoutClick)
                        .padding(vertical = 8.dp)
                )

                // [핵심] 게스트가 아닐 때만 회원탈퇴 버튼 표시 (Visible False 구현)
                if (!isGuest) {
                    Text(
                        text = "회원 탈퇴",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubGray,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToWithdraw)
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

// 정보 한 줄을 표시하는 컴포넌트 (타이틀 --- 여백 --- 데이터)
@Composable
fun UserInfoRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = SubBlack
        )
        // [핵심] 1f 만큼의 가중치를 주어 텍스트 사이를 최대한 벌림
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = SubDarkGray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UserAccountInfoScreenPreview() {
    UserAccountInfoScreen(
        nickname = "김철수",
        isGuest = false,
        onBackClick = {},
        onLogoutClick = {},
        onNavigateToWithdraw = {}
    )
}