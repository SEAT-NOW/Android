package com.gmg.seatnow.presentation.owner.store.mypage.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
fun ChangePasswordScreen(
    uiState: MyPageUiState, // ★ [수정됨] MyPageViewModel 종속성 제거
    onAction: (MyPageAction) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            SeatNowTopAppBar(
                title = "비밀번호 변경",
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

            // 1. [새 비밀번호] 입력 필드
            SeatNowTextField(
                value = uiState.password.newPassword, // ★ [수정됨] password 바구니 참조
                onValueChange = { onAction(MyPageAction.UpdateNewPassword(it)) },
                placeholder = "새 비밀번호 (8~20자리, 영문/숫자/특수기호 포함)",
                isPassword = true,
                errorText = uiState.password.newPasswordError, // ★ [수정됨] password 바구니 참조
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. [새 비밀번호 확인] 입력 필드
            SeatNowTextField(
                value = uiState.password.newPasswordCheck, // ★ [수정됨]
                onValueChange = { onAction(MyPageAction.UpdateNewPasswordCheck(it)) },
                placeholder = "새 비밀번호 확인",
                isPassword = true,
                errorText = uiState.password.newPasswordCheckError, // ★ [수정됨]
                imeAction = ImeAction.Done
            )

            // 버튼 간격 40dp
            Spacer(modifier = Modifier.height(40.dp))

            // 3. [변경] 버튼
            Button(
                onClick = { onAction(MyPageAction.OnChangePasswordClick) },
                enabled = uiState.password.isChangePasswordButtonEnabled && !uiState.isLoading, // ★ [수정됨] (isLoading은 최상위 유지)
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
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "변경",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

// ================= PREVIEWS =================

@Preview(showBackground = true, name = "1. 기본 상태")
@Composable
fun PreviewChangePasswordScreen() {
    SeatNowTheme {
        ChangePasswordScreen(
            uiState = MyPageUiState(),
            onAction = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "2. 에러 상태")
@Composable
fun PreviewChangePasswordScreen_Error() {
    SeatNowTheme {
        ChangePasswordScreen(
            uiState = MyPageUiState(
                password = PasswordState( // ★ [수정됨] Preview도 바구니로 감싸주기
                    newPassword = "123",
                    newPasswordError = "영문, 숫자, 특수문자 포함 8~20자리여야 합니다.",
                    newPasswordCheck = "1234",
                    newPasswordCheckError = "비밀번호가 일치하지 않습니다."
                )
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "3. 완료 가능 상태")
@Composable
fun PreviewChangePasswordScreen_Valid() {
    SeatNowTheme {
        ChangePasswordScreen(
            uiState = MyPageUiState(
                password = PasswordState( // ★ [수정됨] Preview도 바구니로 감싸주기
                    newPassword = "Password123!",
                    newPasswordCheck = "Password123!",
                    isChangePasswordButtonEnabled = true
                )
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}