package com.gmg.seatnow.presentation.owner.store.mypage.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.component.SeatNowTextField
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
// ★ [수정됨] Contract 파일에서 Import 하도록 수정
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageAction
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageUiState
import com.gmg.seatnow.presentation.owner.store.mypage.PasswordState
import com.gmg.seatnow.presentation.theme.*

@Composable
fun CheckPasswordScreen(
    uiState: MyPageUiState, // ★ [수정됨] MyPageViewModel 종속성 제거
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
                value = uiState.password.checkPassword, // ★ [수정됨] password 바구니 참조
                onValueChange = { onAction(MyPageAction.UpdateCheckPassword(it)) },
                placeholder = "비밀번호",
                isPassword = true,
                errorText = uiState.password.checkPasswordError // ★ [수정됨]
            )

            Spacer(modifier = Modifier.height(80.dp))

            // 3. [다음] 버튼
            Button(
                onClick = { onAction(MyPageAction.OnCheckPasswordNextClick) },
                // 8자 이상일 때만 활성화
                enabled = uiState.password.checkPassword.length >= 8, // ★ [수정됨]
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
            uiState = MyPageUiState(
                password = PasswordState( // ★ [수정됨] Preview도 바구니로 감싸주기
                    checkPassword = "wrongpassword",
                    checkPasswordError = "유효하지 않은 비밀번호입니다."
                )
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}