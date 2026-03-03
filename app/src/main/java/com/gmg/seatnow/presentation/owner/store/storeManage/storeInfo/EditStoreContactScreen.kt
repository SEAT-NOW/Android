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
// ★ [수정됨] Contract 파일에서 Import 하도록 수정
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageAction
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageUiState
import com.gmg.seatnow.presentation.owner.store.mypage.StoreInfoState
import com.gmg.seatnow.presentation.theme.*

@Composable
fun EditStoreContactScreen(
    uiState: MyPageUiState, // ★ [수정됨] MyPageViewModel 종속성 제거
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
                value = uiState.storeInfo.editStoreContact, // ★ [수정됨] storeInfo 바구니 참조
                onValueChange = { onAction(MyPageAction.UpdateStoreContactInput(it)) },
                placeholder = "가게 연락처 (숫자만 입력)",
                keyboardType = KeyboardType.Number,
                visualTransformation = NumberVisualTransformation(),
                errorText = uiState.storeInfo.editStoreContactError, // ★ [수정됨]
                isEnabled = !uiState.storeInfo.isStoreContactUpdateSuccess, // ★ [수정됨]
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(40.dp))

            // [변경] 버튼
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onAction(MyPageAction.OnStoreContactConfirmClick)
                },
                // 활성화 조건
                enabled = (uiState.storeInfo.editStoreContact.isNotEmpty() && !uiState.isLoading) || uiState.storeInfo.isStoreContactUpdateSuccess, // ★ [수정됨]
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
                if (uiState.isLoading) { // ★ [수정됨] (isLoading은 최상위 속성)
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
            uiState = MyPageUiState(
                storeInfo = StoreInfoState( // ★ [수정됨] Preview도 바구니로 감싸주기
                    editStoreContact = "0212345678",
                    isStoreContactUpdateSuccess = false
                )
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}