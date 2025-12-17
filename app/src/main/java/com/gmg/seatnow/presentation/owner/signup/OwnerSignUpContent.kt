package com.gmg.seatnow.presentation.owner.signup

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.component.PhoneNumberVisualTransformation
import com.gmg.seatnow.presentation.component.SeatNowTextField
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.component.SignUpTextFieldWithButton
import com.gmg.seatnow.presentation.component.TermItem
import com.gmg.seatnow.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerSignUpContent(
    uiState: OwnerSignUpUiState,
    onAction: (SignUpAction) -> Unit,
    onBackClick: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = uiState.currentStep.progress,
        animationSpec = tween(durationMillis = 500),
        label = "ProgressAnimation"
    )

    Scaffold(
        topBar = {
            Column {
                SeatNowTopAppBar(
                    title = uiState.currentStep.title,
                    onBackClick = onBackClick,
                    topMargin = 15.dp
                )

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .padding(horizontal = 15.dp),
                    color = SubDarkGray,
                    trackColor = SubLightGray,
                    strokeCap = StrokeCap.Round,
                )
            }
        },
        containerColor = White
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            when(uiState.currentStep) {
                SignUpStep.STEP_1_BASIC -> Step1BasicContent(uiState, onAction)
                // 다른 단계는 생략
                else -> Text("준비 중")
            }

            Spacer(modifier = Modifier.height(40.dp))

            // [다음 버튼]
            Button(
                onClick = { onAction(SignUpAction.OnNextClick) },
                enabled = uiState.isNextButtonEnabled,
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
                    text = if(uiState.currentStep == SignUpStep.STEP_6_COMPLETE) "완료" else "다음",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun Step1BasicContent(
    uiState: OwnerSignUpUiState,
    onAction: (SignUpAction) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column {
        // 1. [이메일 입력]
        SignUpTextFieldWithButton(
            value = uiState.email,
            onValueChange = { onAction(SignUpAction.UpdateEmail(it)) },
            placeholder = "이메일",
            buttonText = if (uiState.isEmailVerified) "인증완료" else if (uiState.isEmailCodeSent) "재전송" else "인증번호 전송",
            errorText = uiState.emailError,

            // 입력 활성화: 인증 완료 전까지만 가능
            isEnabled = !uiState.isEmailVerified,

            // 버튼 활성화: 인증 미완료 AND 이메일 입력됨 AND 에러 없음
            isButtonEnabled = !uiState.isEmailVerified && uiState.email.isNotBlank() && uiState.emailError == null,

            onButtonClick = {
                focusManager.clearFocus()
                onAction(SignUpAction.RequestEmailCode)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. [이메일 인증번호]
        SignUpTextFieldWithButton(
            value = uiState.authCode,
            onValueChange = {
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    onAction(SignUpAction.UpdateAuthCode(it))
                }
            },
            placeholder = "인증번호 입력",
            buttonText = "확인",
            timerText = uiState.emailTimerText,

            // ★ [핵심 수정] 입력 활성화 조건:
            // 1) 전송됨(Sent)
            // 2) 아직 인증 완료 안 됨
            // 3) 검증 시도(확인 버튼 클릭) 안 함 -> 확인 누르면 즉시 false가 되어 입력 막힘
            isEnabled = uiState.isEmailCodeSent && !uiState.isEmailVerified && !uiState.isEmailVerificationAttempted,

            // 버튼 활성화 조건:
            // 1) 입력란이 활성화 상태여야 하고
            // 2) 타이머가 끝나지 않았고
            // 3) 6자리 숫자가 다 입력되었을 때
            isButtonEnabled = (uiState.isEmailCodeSent && !uiState.isEmailVerified && !uiState.isEmailVerificationAttempted)
                    && !uiState.isEmailTimerExpired
                    && uiState.authCode.length == 6,

            keyboardType = KeyboardType.Number,
            onButtonClick = {
                focusManager.clearFocus()
                onAction(SignUpAction.VerifyEmailCode)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3. [비밀번호]
        SeatNowTextField(
            value = uiState.password,
            onValueChange = { onAction(SignUpAction.UpdatePassword(it)) },
            placeholder = "비밀번호 (8~20자리, 영문/숫자/특수기호 포함)",
            isPassword = true,
            errorText = uiState.passwordError
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 4. [비밀번호 확인]
        SeatNowTextField(
            value = uiState.passwordCheck,
            onValueChange = { onAction(SignUpAction.UpdatePasswordCheck(it)) },
            placeholder = "비밀번호 확인",
            isPassword = true,
            errorText = uiState.passwordCheckError
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 5. [휴대폰 번호]
        SignUpTextFieldWithButton(
            value = uiState.phone,
            onValueChange = { input ->
                if(input.length <= 11 && input.all { it.isDigit() })
                    onAction(SignUpAction.UpdatePhone(input))
            },
            placeholder = "휴대폰 번호('-' 제외)",
            buttonText = if(uiState.isPhoneVerified) "인증완료" else if(uiState.isPhoneCodeSent) "재전송" else "인증번호 전송",
            keyboardType = KeyboardType.Number,
            visualTransformation = PhoneNumberVisualTransformation(),

            isEnabled = !uiState.isPhoneVerified,

            // 버튼 활성화: 인증 미완료 AND 11자리 입력
            isButtonEnabled = !uiState.isPhoneVerified && uiState.phone.length == 11,

            onButtonClick = {
                focusManager.clearFocus()
                onAction(SignUpAction.RequestPhoneCode)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 6. [휴대폰 인증 번호]
        SignUpTextFieldWithButton(
            value = uiState.phoneAuthCode,
            onValueChange = {
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    onAction(SignUpAction.UpdatePhoneAuthCode(it))
                }
            },
            placeholder = "인증번호 입력",
            buttonText = "확인",
            timerText = uiState.phoneTimerText,
            keyboardType = KeyboardType.Number,

            // ★ [핵심 수정] 입력 활성화 조건
            isEnabled = uiState.isPhoneCodeSent && !uiState.isPhoneVerified && !uiState.isPhoneVerificationAttempted,

            // 버튼 활성화 조건
            isButtonEnabled = (uiState.isPhoneCodeSent && !uiState.isPhoneVerified && !uiState.isPhoneVerificationAttempted)
                    && !uiState.isPhoneTimerExpired
                    && uiState.phoneAuthCode.length == 6,

            onButtonClick = {
                focusManager.clearFocus()
                onAction(SignUpAction.VerifyPhoneCode)
            }
        )

        Spacer(modifier = Modifier.height(30.dp))

        TermsAgreementSection()
    }
}

// 약관 동의 섹션 UI
@Composable
fun TermsAgreementSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 전체 동의
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = false,
                onCheckedChange = {},
                colors = CheckboxDefaults.colors(checkedColor = PointRed)
            )
            Text(
                text = "서비스 이용약관 모두 동의(선택 정보 포함)",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        HorizontalDivider(color = SubLightGray, thickness = 1.dp)

        Spacer(modifier = Modifier.height(8.dp))

        // 개별 약관 아이템들
        TermItem(title = "[필수] 만 14세 이상", showArrow = false)
        TermItem(title = "[필수] 이용약관 동의", showArrow = true)
        TermItem(title = "[필수] 개인정보 수집이용 동의", showArrow = true)
        TermItem(title = "[필수] 개인정보 처리방침 동의", showArrow = true)
    }
}



@Preview(showBackground = true, name = "Step 1 UI", heightDp = 1000)
@Composable
fun PreviewOwnerSignUpContent() {
    MaterialTheme {
        OwnerSignUpContent(
            uiState = OwnerSignUpUiState(
                currentStep = SignUpStep.STEP_1_BASIC
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}