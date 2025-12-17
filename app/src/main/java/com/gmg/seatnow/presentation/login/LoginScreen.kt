package com.gmg.seatnow.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.R
import com.gmg.seatnow.presentation.theme.KakaoYellow
import com.gmg.seatnow.presentation.theme.KakaoYellowPressed
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SeatNowTheme
import com.gmg.seatnow.presentation.theme.SubBlack
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(), // Hilt 주입
    onNavigateToUserMain: () -> Unit,
    onNavigateToOwnerLogin: () -> Unit
) {
    // ViewModel 이벤트 감지
    LaunchedEffect(true) {
        viewModel.event.collect { event ->
            when (event) {
                is LoginViewModel.LoginEvent.NavigateToUserMain -> onNavigateToUserMain()
                is LoginViewModel.LoginEvent.NavigateToOwnerLogin -> onNavigateToOwnerLogin()
            }
        }
    }

    // UI 그리기 (기존 코드와 동일)
    // 버튼 클릭 시 ViewModel 함수 호출
    LoginScreenContent(
        onKakaoLoginClick = viewModel::onKakaoLoginClick,
        onOwnerLoginClick = viewModel::onOwnerLoginClick,
        onGuestLoginClick = viewModel::onGuestLoginClick
    )
}

@Composable
fun LoginScreenContent(
    onKakaoLoginClick: () -> Unit, // 카카오 로그인 버튼 눌렀을 때 동작
    onOwnerLoginClick: () -> Unit,  // 사장님 링크 눌렀을 때 동작
    onGuestLoginClick: () -> Unit // 게스트 로그인 동작
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PointRed) // 배경색 설정
            .systemBarsPadding() // 시스템바 여백 설정
    ) {

        Text(
            text = "둘러보기",
            color = Color.LightGray,
            style = MaterialTheme.typography.bodyLarge, // 적당한 크기
            modifier = Modifier
                .align(Alignment.TopStart) // 왼쪽 위 배치
                .padding(30.dp) // 여백
                .clickable { onGuestLoginClick() }
        )

        // 1. 중앙 내용 (로고 + 텍스트)
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 로고 이미지 (아까 만든 스플래시용 로고 재사용)
            Image(
                painter = painterResource(id = R.drawable.ic_splash_logo),
                contentDescription = "SeatNow Logo",
                modifier = Modifier.width(180.dp) // 시안에 맞춰 크기 조절
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // 로고 아래 설명 텍스트
            Text(
                text = "실시간 대학가 술집 좌석 확인 서비스",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
        }

        // 2. 하단 버튼 영역
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp, start = 20.dp, end = 20.dp), // 양옆, 아래 여백
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 카카오 로그인 버튼
            Button(
                onClick = onKakaoLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
                    .height(50.dp),
                interactionSource = interactionSource,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(isPressed) KakaoYellowPressed else KakaoYellow, // 카카오 노란색
                    contentColor = SubBlack      // 글씨 검은색 (#181717)
                ),
                shape = RoundedCornerShape(12.dp), // 둥근 모서리
            ) {
                Text(
                    text = "카카오 로그인",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // "술집 사장님이신가요?" 링크
            Text(
                text = "술집 사장님이신가요?",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                textDecoration = TextDecoration.Underline, // 밑줄 긋기
                modifier = Modifier.clickable { onOwnerLoginClick() } // 클릭 가능하게
            )
        }


    }
}

// === 미리보기 (Preview) ===
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    SeatNowTheme {
        LoginScreenContent(
            onKakaoLoginClick = {},
            onOwnerLoginClick = {},
            onGuestLoginClick = {}
        )
    }
}