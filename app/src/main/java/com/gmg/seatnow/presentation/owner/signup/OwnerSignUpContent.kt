package com.gmg.seatnow.presentation.owner.signup

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpViewModel.OwnerSignUpUiState
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpViewModel.SignUpAction
import com.gmg.seatnow.presentation.owner.signup.steps.Step1BasicScreen
import com.gmg.seatnow.presentation.owner.signup.steps.Step2BusinessInfoScreen
import com.gmg.seatnow.presentation.owner.signup.steps.TermsDetailScreen
import com.gmg.seatnow.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerSignUpContent(
    uiState: OwnerSignUpUiState,
    onAction: (SignUpAction) -> Unit,
    onBackClick: () -> Unit
) {
    // 상세 화면이 열려있으면 뒤로가기 키를 눌렀을 때 상세 화면만 닫음
    BackHandler(enabled = uiState.openedTermType != null) {
        onAction(SignUpAction.CloseTermDetail)
    }

    // ★ 상태에 따라 [회원가입 폼] <-> [약관 상세 화면] 전환
    Crossfade(targetState = uiState.openedTermType, label = "TermDetailTransition") { termType ->
        if (termType != null) {
            // 약관 상세 화면 보여주기
            TermsDetailScreen(
                termType = termType,
                onBackClick = { onAction(SignUpAction.CloseTermDetail) }
            )
        } else {
            // 기존 회원가입 폼 보여주기
            SignUpFormScreen(uiState, onAction, onBackClick)
        }
    }
}

@Composable
fun SignUpFormScreen(
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            when(uiState.currentStep) {
                SignUpStep.STEP_1_BASIC -> Step1BasicScreen(uiState, onAction)
                SignUpStep.STEP_2_BUSINESS -> Step2BusinessInfoScreen(uiState, onAction)
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



@Preview(showBackground = true, name = "Step 1 UI")
@Composable
fun PreviewOwnerSignUpStep1Content() {
    SeatNowTheme {
        OwnerSignUpContent(
            uiState = OwnerSignUpUiState(
                currentStep = SignUpStep.STEP_1_BASIC
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Step 2 UI")
@Composable
fun PreviewOwnerSignUpStep2Content() {
    SeatNowTheme {
        OwnerSignUpContent(
            uiState = OwnerSignUpUiState(
                currentStep = SignUpStep.STEP_2_BUSINESS
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}

