package com.gmg.seatnow.presentation.owner.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.component.SeatNowTextField
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.PointRedPressed
import com.gmg.seatnow.presentation.theme.SeatNowTheme
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
    onNavigateToSignUp: () -> Unit,
    onNavigateToFindEmail: () -> Unit,
    onNavigateToFindPassword: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // 이벤트 처리
    LaunchedEffect(true) {
        viewModel.event.collect { event ->
            when(event) {
                is OwnerLoginEvent.NavigateToOwnerMain -> onNavigateToOwnerMain()
                is OwnerLoginEvent.NavigateToSignUp -> onNavigateToSignUp()
                is OwnerLoginEvent.NavigateToFindEmail -> onNavigateToFindEmail()
                is OwnerLoginEvent.NavigateToFindPassword -> onNavigateToFindPassword()
            }
        }
    }

    // UI(Stateless) 호출
    OwnerLoginContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onBackClick = onBackClick
    )
}

// 2. [Stateless] 순수 UI 컴포넌트 (여기에는 ViewModel이 없어서 미리보기가 가능함)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerLoginContent(
    uiState: OwnerLoginUiState,
    onAction: (OwnerLoginAction) -> Unit,
    onBackClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Scaffold(
        topBar = {
            SeatNowTopAppBar(
                title = "사장님 로그인",
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

            // 이메일 입력
            SeatNowTextField(
                value = uiState.email,
                onValueChange = { onAction(OwnerLoginAction.UpdateEmail(it)) },
                placeholder = "이메일",
                errorText = uiState.emailError
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 비밀번호 입력
            SeatNowTextField(
                value = uiState.password,
                onValueChange = { onAction(OwnerLoginAction.UpdatePassword(it)) },
                placeholder = "비밀번호 (8~20자리, 영문/숫자/특수기호 포함)",
                isPassword = true,
                imeAction = ImeAction.Done,
                errorText = uiState.passwordError
            )

            Spacer(modifier = Modifier.height(24.dp))

            // [로그인 실패 시 버튼 위에 뜨는 에러 메시지]
            if (uiState.loginError != null) {
                Text(
                    text = uiState.loginError,
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Red, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(36.dp)) // 에러 없을 때 높이 맞춤용
            }

            // 로그인 버튼
            Button(
                onClick = { onAction(OwnerLoginAction.OnLoginClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 10.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp),
                enabled = uiState.isLoginButtonEnabled,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "이메일 찾기",
                    fontSize = 14.sp,
                    color = SubGray, // 혹은 위험한 작업임을 알리기 위해 Red 계열 사용 가능
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { onAction(OwnerLoginAction.OnFindEmailClick) }
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.width(32.dp))

                Text(
                    text = "비밀번호 찾기",
                    fontSize = 14.sp,
                    color = SubGray,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { onAction(OwnerLoginAction.OnFindPasswordClick) }
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 회원가입 버튼
            OutlinedButton(
                onClick = { onAction(OwnerLoginAction.OnSignUpClick) },
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
            uiState = OwnerLoginUiState(),
            onAction = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State UI")
@Composable
fun PreviewOwnerLoginScreenError() {
    SeatNowTheme{
        OwnerLoginContent(
            uiState = OwnerLoginUiState(
                email = "test@",
                emailError = "이메일 형식이 올바르지 않습니다.",
                password = "123",
                passwordError = "비밀번호를 확인해주세요.",
                loginError = "아이디 또는 비밀번호가 일치하지 않습니다.",
                isLoginButtonEnabled = true
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}