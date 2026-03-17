package com.gmg.seatnow.presentation.owner.login

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.component.NumberVisualTransformation
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.component.SignUpTextFieldWithButton
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.PointRedPressed
import com.gmg.seatnow.presentation.theme.SubLightGray
import com.gmg.seatnow.presentation.theme.SubPaleGray
import com.gmg.seatnow.presentation.theme.White
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun OwnerFindEmailScreen(
    onBackClick: () -> Unit,
    viewModel: OwnerFindEmailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 뷰모델 이벤트에 따른 Toast 표시 등을 위한 Effect
    LaunchedEffect(true) {
        viewModel.event.collect { event ->
            when (event) {
                is OwnerFindEmailEvent.NavigateBack -> onBackClick()
                is OwnerFindEmailEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    OwnerFindEmailContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerFindEmailContent(
    uiState: OwnerFindEmailUiState,
    onAction: (OwnerFindEmailAction) -> Unit,
    onBackClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 휴대폰 인증이 '성공'으로 바뀌는 순간 포커스 해제
    LaunchedEffect(uiState.isPhoneVerified) {
        if (uiState.isPhoneVerified) focusManager.clearFocus()
    }

    Scaffold(
        topBar = {
            SeatNowTopAppBar(
                title = "이메일 찾기",
                onBackClick = onBackClick,
                modifier = Modifier.padding(top = 0.dp),
                topMargin = 15.dp
            )
        },
        containerColor = White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 1. [휴대폰 번호]
            SignUpTextFieldWithButton(
                value = uiState.phone,
                onValueChange = { onAction(OwnerFindEmailAction.UpdatePhone(it)) },
                placeholder = "휴대폰 번호('-' 제외)",
                buttonText = if(uiState.isPhoneVerified) "인증완료" else if(uiState.isPhoneCodeSent) "재전송" else "인증번호 전송",
                keyboardType = KeyboardType.Number,
                visualTransformation = NumberVisualTransformation(),
                errorText = uiState.phoneError,
                isEnabled = !uiState.isPhoneVerified,
                isButtonEnabled = !uiState.isPhoneVerified && uiState.phone.length == 11,
                onButtonClick = {
                    focusManager.clearFocus()
                    onAction(OwnerFindEmailAction.RequestPhoneCode)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. [휴대폰 인증 번호]
            SignUpTextFieldWithButton(
                value = uiState.phoneAuthCode,
                onValueChange = { onAction(OwnerFindEmailAction.UpdatePhoneAuthCode(it)) },
                placeholder = "인증번호 입력",
                buttonText = "확인",
                timerText = uiState.phoneTimerText,
                keyboardType = KeyboardType.Number,
                errorText = uiState.phoneVerifiedError,
                isEnabled = uiState.isPhoneCodeSent && !uiState.isPhoneVerified && !uiState.isPhoneVerificationAttempted,
                isButtonEnabled = (uiState.isPhoneCodeSent && !uiState.isPhoneVerified && !uiState.isPhoneVerificationAttempted)
                        && !uiState.isPhoneTimerExpired
                        && uiState.phoneAuthCode.length == 6,
                onButtonClick = {
                    focusManager.clearFocus()
                    onAction(OwnerFindEmailAction.VerifyPhoneCode)
                }
            )

            // 하단 여백: 버튼 하단까지 쭉 밀기 위함
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onAction(OwnerFindEmailAction.OnNextClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 10.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                enabled = uiState.isNextButtonEnabled,
                interactionSource = interactionSource,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPressed) PointRedPressed else PointRed,
                    disabledContainerColor = SubLightGray,
                    contentColor = if (isPressed) SubLightGray else SubPaleGray,
                    disabledContentColor = White
                )
            ) {
                Text(
                    text = "다음",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // 요청하신 바닥에서 띄우는 Spacer (로그인과 높이를 맞추고 싶으시다면 여기를 조정)
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
