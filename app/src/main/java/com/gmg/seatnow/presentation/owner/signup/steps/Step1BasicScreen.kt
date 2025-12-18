package com.gmg.seatnow.presentation.owner.signup.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmg.seatnow.presentation.component.PhoneNumberVisualTransformation
import com.gmg.seatnow.presentation.component.SeatNowTextField
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.component.SignUpTextFieldWithButton
import com.gmg.seatnow.presentation.component.TermItem
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpUiState
import com.gmg.seatnow.presentation.owner.signup.SignUpAction
import com.gmg.seatnow.presentation.owner.signup.TermType
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SeatNowTheme
import com.gmg.seatnow.presentation.theme.SubBlack
import com.gmg.seatnow.presentation.theme.SubLightGray
import com.gmg.seatnow.presentation.theme.White

@Composable
fun Step1BasicScreen(
    uiState: OwnerSignUpUiState,
    onAction: (SignUpAction) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column {
        // ... (이메일, 비밀번호, 휴대폰 입력 필드 코드는 기존과 동일하게 유지 - 분량상 생략하지 않고 모두 적어드립니다) ...
        // 1. [이메일 입력]
        SignUpTextFieldWithButton(
            value = uiState.email,
            onValueChange = { onAction(SignUpAction.UpdateEmail(it)) },
            placeholder = "이메일",
            buttonText = if (uiState.isEmailVerified) "인증완료" else if (uiState.isEmailCodeSent) "재전송" else "인증번호 전송",
            errorText = uiState.emailError,
            isEnabled = !uiState.isEmailVerified,
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
            isEnabled = uiState.isEmailCodeSent && !uiState.isEmailVerified && !uiState.isEmailVerificationAttempted,
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
            isEnabled = uiState.isPhoneCodeSent && !uiState.isPhoneVerified && !uiState.isPhoneVerificationAttempted,
            isButtonEnabled = (uiState.isPhoneCodeSent && !uiState.isPhoneVerified && !uiState.isPhoneVerificationAttempted)
                    && !uiState.isPhoneTimerExpired
                    && uiState.phoneAuthCode.length == 6,
            onButtonClick = {
                focusManager.clearFocus()
                onAction(SignUpAction.VerifyPhoneCode)
            }
        )

        Spacer(modifier = Modifier.height(30.dp))

        // 7. 약관 동의 (연결)
        TermsAgreementSection(uiState, onAction)
    }
}

// 약관 동의 섹션 UI (ViewModel과 연결됨)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAgreementSection(
    uiState: OwnerSignUpUiState,
    onAction: (SignUpAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 전체 동의
        Row(
            modifier = Modifier
                .fillMaxWidth() // ★ 1. 가로로 꽉 채우기
                .clickable { onAction(SignUpAction.ToggleAllTerms(!uiState.isAllTermsAgreed)) }
                .padding(bottom = 8.dp)
            , // 터치 영역 위아래 여백 (선택사항)
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center // ★ 2. 내용물을 가운데 정렬
        ) {
            CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                Checkbox(
                    checked = uiState.isAllTermsAgreed,
                    onCheckedChange = { onAction(SignUpAction.ToggleAllTerms(it)) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PointRed,
                        uncheckedColor = SubLightGray,
                        checkmarkColor = White
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(
                text = "서비스 이용약관 모두 동의(선택 정보 포함)",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        HorizontalDivider(color = SubLightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 15.dp))
        Spacer(modifier = Modifier.height(8.dp))

        // 개별 약관 아이템들
        TermItem(
            title = TermType.AGE.title,
            isChecked = uiState.isAgeVerified,
            showArrow = false,
            onToggle = { onAction(SignUpAction.ToggleTerm(TermType.AGE)) }
        )
        TermItem(
            title = TermType.SERVICE.title,
            isChecked = uiState.isServiceVerified,
            showArrow = true,
            onToggle = { onAction(SignUpAction.ToggleTerm(TermType.SERVICE)) },
            onDetailClick = { onAction(SignUpAction.OpenTermDetail(TermType.SERVICE)) }
        )
        TermItem(
            title = TermType.PRIVACY_COLLECT.title,
            isChecked = uiState.isPrivacyCollectVerified,
            showArrow = true,
            onToggle = { onAction(SignUpAction.ToggleTerm(TermType.PRIVACY_COLLECT)) },
            onDetailClick = { onAction(SignUpAction.OpenTermDetail(TermType.PRIVACY_COLLECT)) }
        )
        TermItem(
            title = TermType.PRIVACY_PROVIDE.title,
            isChecked = uiState.isPrivacyProvideVerified,
            showArrow = true,
            onToggle = { onAction(SignUpAction.ToggleTerm(TermType.PRIVACY_PROVIDE)) },
            onDetailClick = { onAction(SignUpAction.OpenTermDetail(TermType.PRIVACY_PROVIDE)) }
        )
    }
}

// ★ 약관 상세 화면 (요청하신 이미지 스타일 적용)
@Composable
fun TermsDetailScreen(
    termType: TermType,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            SeatNowTopAppBar(
                title = termType.title.replace("[필수]", "").trim(), // "[필수]" 제거하고 제목 표시
                onBackClick = onBackClick,
                topMargin = 15.dp
            )
        },
        containerColor = White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // 임의의 약관 내용 생성 (Mocking)
            val content = when(termType) {
                TermType.SERVICE -> getServiceTermsMock()
                TermType.PRIVACY_COLLECT -> getPrivacyCollectMock()
                TermType.PRIVACY_PROVIDE -> getPrivacyProvideMock()
                else -> "내용이 없습니다."
            }

            Text(text = content, style = MaterialTheme.typography.bodyMedium, color = SubBlack, lineHeight = 24.sp)
        }
    }
}

// Mock Data Generators
fun getServiceTermsMock() = """
    제1조 (목적)
    이 약관은 SeatNow(이하 "회사")가 제공하는 서비스 이용조건 및 절차, 회사와 회원 간의 권리, 의무 및 책임사항 등을 규정함을 목적으로 합니다.
    
    제2조 (정의)
    1. "서비스"란 회사가 제공하는 공간 예약 및 관리 플랫폼을 의미합니다.
    2. "회원"이란 이 약관에 동의하고 회사가 제공하는 서비스를 이용하는 자를 말합니다.
    
    제3조 (약관의 게시와 개정)
    회사는 이 약관의 내용을 회원이 쉽게 알 수 있도록 서비스 초기 화면에 게시합니다.
""".trimIndent()

fun getPrivacyCollectMock() = """
    1. 수집하는 개인정보 항목
    이름, 이메일 주소, 비밀번호, 휴대전화번호, 사업자등록번호
    
    2. 수집 및 이용 목적
    회원 가입 의사 확인, 본인 식별 및 인증, 회원 자격 유지 및 관리
    
    3. 보유 및 이용 기간
    회원 탈퇴 시까지 (단, 관계 법령에 따릅니다)
""".trimIndent()

fun getPrivacyProvideMock() = """
    1. 개인정보 제3자 제공
    회사는 원칙적으로 이용자의 개인정보를 외부에 제공하지 않습니다. 다만, 아래의 경우에는 예외로 합니다.
    
    - 이용자들이 사전에 동의한 경우
    - 법령의 규정에 의거하거나, 수사 목적으로 법령에 정해진 절차와 방법에 따라 수사기관의 요구가 있는 경우
""".trimIndent()

@Preview(showBackground = true, name = "Step 1 Only", heightDp = 800)
@Composable
fun PreviewStep1BasicScreen() {
    SeatNowTheme {
        Step1BasicScreen(
            uiState = OwnerSignUpUiState(),
            onAction = {}
        )
    }
}
