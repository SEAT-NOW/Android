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
import com.gmg.seatnow.presentation.user.mypage.UserInfoRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountInfoScreen(
    uiState: MyPageViewModel.MyPageUiState,
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
        Column(
            modifier = Modifier
                .fillMaxSize() // 전체 크기 채우기
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            UserInfoRow(
                title = "휴대폰 번호",
                value = if (uiState.isProfileLoaded) uiState.ownerPhoneNumber else "불러오는 중..."
            )

            Spacer(modifier = Modifier.height(24.dp))

            UserInfoRow(
                title = "이메일",
                value = if (uiState.isProfileLoaded) uiState.ownerEmail else "불러오는 중..."
            )

            Spacer(modifier = Modifier.height(24.dp))

            UserInfoRow(
                title = "비밀번호",
                value = "●●●●●●●", // 실제 값 대신 마스킹 처리
                showArrow = true   // "눌러서 변경 가능함"을 암시
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "회원 탈퇴",
                    fontSize = 14.sp,
                    color = SubGray, // 혹은 위험한 작업임을 알리기 위해 Red 계열 사용 가능
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable(onClick = onNavigateToWithdraw)
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.width(32.dp))

                Text(
                    text = "로그아웃",
                    fontSize = 14.sp,
                    color = SubGray,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable(onClick = onLogoutClick)
                        .padding(8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AccountInfoScreenPreview() {
    // 프리뷰용 더미 상태 생성
    val dummyState = MyPageViewModel.MyPageUiState(
        isLoading = false,
        ownerEmail = "test@seatnow.com",
        ownerPhoneNumber = "010-1234-5678",
        isProfileLoaded = true
    )

    AccountInfoScreen(
        uiState = dummyState,
        onBackClick = {},
        onLogoutClick = {},
        onNavigateToWithdraw = {}
    )
}