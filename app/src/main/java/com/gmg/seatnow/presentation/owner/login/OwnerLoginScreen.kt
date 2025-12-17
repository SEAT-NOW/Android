package com.gmg.seatnow.presentation.owner.login

import android.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.component.SeatNowTextField
import com.gmg.seatnow.presentation.extension.bottomShadow
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.PointRedPressed
import com.gmg.seatnow.presentation.theme.SeatNowTheme
import com.gmg.seatnow.presentation.theme.SubBlack
import com.gmg.seatnow.presentation.theme.SubDarkGray
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.SubLightGray
import com.gmg.seatnow.presentation.theme.SubPaleGray
import com.gmg.seatnow.presentation.theme.White

// 1. [Stateful] 로직과 데이터를 연결하는 상위 컴포넌트
@Composable
fun OwnerLoginScreen(
    viewModel: OwnerLoginViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateToOwnerMain: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val loginError by viewModel.loginError.collectAsState()
    val isButtonEnabled by viewModel.isLoginButtonEnabled.collectAsState()

    // 이벤트 처리
    LaunchedEffect(true) {
        viewModel.event.collect { event ->
            when(event) {
                is OwnerLoginViewModel.OwnerLoginEvent.NavigateToOwnerMain -> onNavigateToOwnerMain()
                is OwnerLoginViewModel.OwnerLoginEvent.NavigateToSignUp -> onNavigateToSignUp()
            }
        }
    }

    // UI(Stateless) 호출
    OwnerLoginContent(
        email = email,
        onEmailChange = viewModel::onEmailChange,
        password = password,
        onPasswordChange = viewModel::onPasswordChange,
        emailError = emailError,
        passwordError = passwordError,
        loginError = loginError,
        isButtonEnabled = isButtonEnabled,
        onLoginClick = viewModel::onLoginClick,
        onSignUpClick = viewModel::onSignUpClick,
        onBackClick = onBackClick
    )
}

// 2. [Stateless] 순수 UI 컴포넌트 (여기에는 ViewModel이 없어서 미리보기가 가능함)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerLoginContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    emailError: String?,
    passwordError: String?,
    loginError: String?,
    isButtonEnabled: Boolean,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "사장님 로그인",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.offset(x = (-6).dp))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "뒤로가기",
                            modifier = Modifier.size(32.dp)) // 꺽새 크기 살짝 키움)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = White)
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
            Spacer(modifier = Modifier.height(30.dp))

            // 이메일 입력
            SeatNowTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = "이메일",
                errorText = emailError
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 비밀번호 입력
            SeatNowTextField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = "비밀번호 (8~20자리, 영문/숫자/특수기호 포함)",
                isPassword = true,
                imeAction = ImeAction.Done,
                errorText = passwordError
            )

            Spacer(modifier = Modifier.height(24.dp))

            // [로그인 실패 시 버튼 위에 뜨는 에러 메시지]
            if (loginError != null) {
                Text(
                    text = loginError,
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Red, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(36.dp)) // 에러 없을 때 높이 맞춤용
            }

            // 로그인 버튼
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 10.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                enabled = isButtonEnabled,
                interactionSource = interactionSource,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPressed) PointRedPressed else PointRed,
                    disabledContainerColor = SubLightGray,
                    contentColor = if (isPressed) SubLightGray else SubPaleGray,
                    disabledContentColor = White
                )
            ) {
                Text(text = "로그인",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 안내 문구
            Text(
                text = "이메일 분실 및 비밀번호 재발급은\n고객센터 이메일로 문의를 부탁드립니다.",
                style = MaterialTheme.typography.labelSmall,
                color = SubGray,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 회원가입 버튼
            OutlinedButton(
                onClick = onSignUpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 10.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, SubDarkGray),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = White,
                    contentColor = SubDarkGray
                )
            ) {
                Text(text = "회원가입",style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}


// 4. [Preview] 미리보기 전용 (이게 빠져서 안 보였던 것입니다)
@Preview(showBackground = true, name = "Default Login UI")
@Composable
fun PreviewOwnerLoginScreen() {
    // 뷰모델 없이 순수 UI만 테스트
    SeatNowTheme {
        OwnerLoginContent(
            email = "",
            onEmailChange = {},
            password = "",
            onPasswordChange = {},
            emailError = null,
            passwordError = null,
            loginError = null,
            isButtonEnabled = false,
            onLoginClick = {},
            onSignUpClick = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State UI")
@Composable
fun PreviewOwnerLoginScreenError() {
    SeatNowTheme{
        OwnerLoginContent(
            email = "test@",
            onEmailChange = {},
            password = "123",
            onPasswordChange = {},
            emailError = "이메일 형식이 올바르지 않습니다.",
            passwordError = "비밀번호를 확인해주세요.",
            loginError = "아이디 또는 비밀번호가 일치하지 않습니다.",
            isButtonEnabled = true,
            onLoginClick = {},
            onSignUpClick = {},
            onBackClick = {}
        )
    }
}