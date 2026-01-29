package com.gmg.seatnow.presentation.owner.store.mypage.store

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.component.NumberVisualTransformation
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.component.SignUpTextFieldWithButton
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
            Spacer(modifier = Modifier.height(24.dp))


            // 1. [가게 연락처] 입력 필드 (버튼 포함 컴포넌트 사용)
            SignUpTextFieldWithButton(
                value = uiState.editStoreContact,
                onValueChange = { onAction(MyPageAction.UpdateStoreContactInput(it)) },
                placeholder = "가게 연락처 (숫자만 입력)",

                // 성공하면 '완료', 아니면 '확인'
                buttonText = if (uiState.isStoreContactUpdateSuccess) "완료" else "확인",

                errorText = uiState.editStoreContactError,

                // 성공 시 필드 입력 비활성화
                isEnabled = !uiState.isStoreContactUpdateSuccess,

                // 버튼 활성화: 성공 안 했고, 입력값 있고, 로딩 아닐 때 (성공 시엔 비활성 or 완료 상태로 둠)
                // 여기서는 Step2 로직대로 성공 후엔 버튼도 비활성화 처리됨 (컴포넌트 내부 로직)
                isButtonEnabled = !uiState.isStoreContactUpdateSuccess
                        && uiState.editStoreContact.isNotEmpty()
                        && !uiState.isLoading,

                keyboardType = KeyboardType.Number,
                visualTransformation = NumberVisualTransformation(), // 공용 포맷터 사용

                onButtonClick = {
                    focusManager.clearFocus()
                    onAction(MyPageAction.OnStoreContactConfirmClick)
                }
            )

            // 버튼을 아래로 밀기 위한 Spacer
            Spacer(modifier = Modifier.height(80.dp))

            // 2. [하단 변경] 버튼
            // 상단 인라인 버튼과 동일한 동작을 수행하거나, 성공 후 나가기 역할을 합니다.
            Button(
                onClick = {
                    if (uiState.isStoreContactUpdateSuccess) {
                        onBackClick() // 성공 상태면 뒤로가기
                    } else {
                        focusManager.clearFocus()
                        onAction(MyPageAction.OnStoreContactConfirmClick) // API 호출
                    }
                },
                // 활성화 조건: (입력값 존재 AND 로딩 X) OR 이미 성공함
                enabled = (uiState.editStoreContact.isNotEmpty() && !uiState.isLoading) || uiState.isStoreContactUpdateSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    // 성공 시 색상을 변경하여 완료됨을 표시 (선택 사항)
                    containerColor = if (uiState.isStoreContactUpdateSuccess) SubGray else PointRed,
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
                        // 성공 여부에 따라 텍스트 변경
                        text = if (uiState.isStoreContactUpdateSuccess) "완료" else "변경",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ================= PREVIEW =================

@Preview(showBackground = true)
@Composable
fun PreviewEditStoreContactScreen() {
    SeatNowTheme {
        EditStoreContactScreen(
            uiState = MyPageViewModel.MyPageUiState(
                editStoreContact = "01012345678",
                isStoreContactUpdateSuccess = false
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}