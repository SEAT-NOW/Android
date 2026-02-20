package com.gmg.seatnow.presentation.owner.store.mypage.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.component.SeatNowTextField // 기존 컴포넌트 임포트
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageAction
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageViewModel
import com.gmg.seatnow.presentation.theme.*

@Composable
fun CheckPasswordScreen(
    uiState: MyPageViewModel.MyPageUiState,
    onAction: (MyPageAction) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            SeatNowTopAppBar(
                title = "비밀번호 확인",
                onBackClick = onBackClick
            )
        },
        containerColor = White,
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. 안내 문구
            Text(
                text = "보안을 위해 비밀번호를 입력해 주세요.",
                style = MaterialTheme.typography.bodyLarge,
                color = SubBlack
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. [비밀번호] 입력 필드
            SeatNowTextField(
                value = uiState.checkPassword,
                onValueChange = { onAction(MyPageAction.UpdateCheckPassword(it)) },
                placeholder = "비밀번호",
                isPassword = true,
                errorText = uiState.checkPasswordError
            )

            Spacer(modifier = Modifier.height(80.dp))

            // 3. [다음] 버튼
            Button(
                onClick = { onAction(MyPageAction.OnCheckPasswordNextClick) },
                // 8자 이상일 때만 활성화
                enabled = uiState.checkPassword.length >= 8,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PointRed,
                    disabledContainerColor = SubLightGray,
                    contentColor = White,
                    disabledContentColor = White
                )
            ) {
                Text(
                    text = "다음",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "3. 에러 발생 (400)")
@Composable
fun PreviewCheckPasswordScreen_Error() {
    SeatNowTheme {
        CheckPasswordScreen(
            uiState = MyPageViewModel.MyPageUiState(
                checkPassword = "wrongpassword",
                checkPasswordError = "유효하지 않은 비밀번호입니다." // 에러 메시지 표시
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}