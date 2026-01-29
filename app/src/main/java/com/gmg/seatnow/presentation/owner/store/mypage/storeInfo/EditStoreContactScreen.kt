package com.gmg.seatnow.presentation.owner.store.mypage.store

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.component.NumberVisualTransformation
import com.gmg.seatnow.presentation.component.SeatNowTextField
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageAction
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageViewModel
import com.gmg.seatnow.presentation.theme.*

@Composable
fun EditStoreContactScreen(
    uiState: MyPageViewModel.MyPageUiState,
    onAction: (MyPageAction) -> Unit,
    onBackClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            SeatNowTopAppBar(
                title = "가게 연락처 수정",
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
            Spacer(modifier = Modifier.height(12.dp))

            SeatNowTextField(
                value = uiState.editStoreContact,
                onValueChange = { onAction(MyPageAction.UpdateStoreContactInput(it)) },
                placeholder = "가게 연락처 (숫자만 입력)",
                keyboardType = KeyboardType.Number,
                visualTransformation = NumberVisualTransformation(),
                errorText = uiState.editStoreContactError,
                // 성공 시 비활성화되지만, 곧바로 화면 이동되므로 큰 의미는 없음
                isEnabled = !uiState.isStoreContactUpdateSuccess,
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(40.dp))

            // [변경] 버튼
            Button(
                onClick = {
                    // ★ 수정: 버튼 클릭 시 무조건 API 호출 시도
                    // (성공 시 ViewModel에서 NavigateBack 이벤트를 보내 화면을 닫음)
                    focusManager.clearFocus()
                    onAction(MyPageAction.OnStoreContactConfirmClick)
                },
                // 활성화 조건: (입력값 존재 AND 로딩 X) OR 이미 성공함
                enabled = (uiState.editStoreContact.isNotEmpty() && !uiState.isLoading) || uiState.isStoreContactUpdateSuccess,
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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEditStoreContactScreen() {
    SeatNowTheme {
        EditStoreContactScreen(
            uiState = MyPageViewModel.MyPageUiState(
                editStoreContact = "0212345678",
                isStoreContactUpdateSuccess = false
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}