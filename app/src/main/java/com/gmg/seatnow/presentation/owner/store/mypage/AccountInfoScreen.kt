package com.gmg.seatnow.presentation.owner.store.mypage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountInfoScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateToWithdraw: () -> Unit
) {
    Scaffold(
        topBar = {
            SeatNowTopAppBar(
                title = "계정 정보 수정",
                onBackClick = onBackClick
            )
        },
        containerColor = White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            // 로그아웃 / 회원탈퇴 버튼만 남김
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "로그아웃",
                    fontSize = 14.sp,
                    color = SubGray,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable(onClick = onLogoutClick)
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.width(32.dp))

                Text(
                    text = "회원 탈퇴",
                    fontSize = 14.sp,
                    color = SubGray, // 혹은 위험한 작업임을 알리기 위해 Red 계열 사용 가능
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable(onClick = onNavigateToWithdraw)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountInfoScreenPreview() {
    AccountInfoScreen(
        onBackClick = {},
        onLogoutClick = {},
        onNavigateToWithdraw = {}
    )
}
