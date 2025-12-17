package com.gmg.seatnow.presentation.owner.signup

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.component.PhoneNumberVisualTransformation
import com.gmg.seatnow.presentation.component.SeatNowTextField
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.component.SignUpTextFieldWithButton
import com.gmg.seatnow.presentation.component.TermItem
import com.gmg.seatnow.presentation.extension.bottomShadow
import com.gmg.seatnow.presentation.theme.*

// ★ 만약 빨간줄이 뜨면 패키지명에 맞게 Import 해주세요 (Alt+Enter)
// import com.gmg.seatnow.presentation.component.SignUpTextFieldWithButton
// import com.gmg.seatnow.presentation.component.SeatNowSimpleTextField
// import com.gmg.seatnow.presentation.component.TermsAgreementSection

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
                // ★ 1. 표준화된 AppBar 사용 (+ 상단 여백 20dp 예시)
                SeatNowTopAppBar(
                    title = uiState.currentStep.title,
                    onBackClick = onBackClick,
                    topMargin = 15.dp
                )

                // ★ 2. ProgressBar 굵기 줄이기 (height 조절)
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp) // ★ 기존 4dp -> 2dp로 얇게 변경
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
                SignUpStep.STEP_2_BUSINESS -> Text("2단계: 사업자 정보 입력")
                SignUpStep.STEP_3_STORE -> Text("3단계: 공간 테이블 구성")
                SignUpStep.STEP_4_OPERATION -> Text("4단계: 운영 정보")
                SignUpStep.STEP_5_SAVESTOREPHOTO -> Text("5단계: 사진 등록")
                SignUpStep.STEP_6_COMPLETE -> Text("회원가입 완료")
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { onAction(SignUpAction.OnNextClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PointRed)
            ) {
                Text(
                    text = if(uiState.currentStep == SignUpStep.STEP_6_COMPLETE) "완료" else "다음",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = White
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
    Column {
        // 1. 이메일
        SignUpTextFieldWithButton(
            value = uiState.email,
            onValueChange = { onAction(SignUpAction.UpdateEmail(it)) },
            placeholder = "이메일",
            buttonText = "인증번호 전송",
            errorText = uiState.emailError,
            onButtonClick = { /* 인증번호 전송 로직 */ }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. 인증번호
        SignUpTextFieldWithButton(
            value = uiState.authCode,
            onValueChange = { onAction(SignUpAction.UpdateAuthCode(it)) },
            placeholder = "인증번호 입력",
            buttonText = "확인",
            buttonColor = PointRed,
            buttonTextColor = White,
            timerText = "3:00",
            onButtonClick = { /* 인증번호 확인 로직 */ }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3. 비밀번호
        SeatNowTextField(
            value = uiState.password,
            onValueChange = { onAction(SignUpAction.UpdatePassword(it)) },
            placeholder = "비밀번호 (8~20자리, 영문/숫자/특수기호 포함)",
            isPassword = true,
            errorText = uiState.passwordError
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 4. 비밀번호 확인 (필요 시 추가)
        SeatNowTextField(
            value = uiState.passwordCheck,
            onValueChange = { onAction(SignUpAction.UpdatePasswordCheck(it)) },
            placeholder = "비밀번호 확인",
            isPassword = true,
            errorText = uiState.passwordCheckError
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 5. 휴대폰 번호 (필요 시 추가)
        SignUpTextFieldWithButton(
            value = uiState.phone,
            onValueChange = { input ->
                if(input.length <= 11 && input.all { it.isDigit() })
                    onAction(SignUpAction.UpdatePhone(input))
            },
            placeholder = "휴대폰 번호('-' 제외)",
            buttonText = "인증번호 전송",
            keyboardType = KeyboardType.Number,
            visualTransformation = PhoneNumberVisualTransformation(),
            onButtonClick = { }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 6. 휴대폰 인증 번호
        SignUpTextFieldWithButton(
            value = uiState.authCode,
            onValueChange = { onAction(SignUpAction.UpdateAuthCode(it)) },
            placeholder = "인증번호 입력",
            buttonText = "확인",
            buttonColor = PointRed,
            buttonTextColor = White,
            timerText = "3:00",
            onButtonClick = { /* 인증번호 확인 로직 */ }
        )

        Spacer(modifier = Modifier.height(30.dp))

        // 6. 약관 동의
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