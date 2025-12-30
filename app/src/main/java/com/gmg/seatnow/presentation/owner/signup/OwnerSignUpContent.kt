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
import androidx.compose.ui.zIndex
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpViewModel.OwnerSignUpUiState
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpViewModel.SignUpAction
import com.gmg.seatnow.presentation.owner.signup.steps.Step1BasicScreen
import com.gmg.seatnow.presentation.owner.signup.steps.Step2BusinessScreen
import com.gmg.seatnow.presentation.owner.signup.steps.Step3StoreScreen
import com.gmg.seatnow.presentation.owner.signup.steps.Step4OperatingScreen
import com.gmg.seatnow.presentation.owner.signup.steps.Step5PhotoScreen
import com.gmg.seatnow.presentation.owner.signup.steps.Step6CompleteScreen
import com.gmg.seatnow.presentation.owner.signup.steps.TermsDetailScreen
import com.gmg.seatnow.presentation.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerSignUpContent(
    uiState: OwnerSignUpUiState,
    onAction: (SignUpAction) -> Unit,
    onBackClick: () -> Unit
) {
    //주소 검색 열려있으면 닫기 (최우선순위)
    BackHandler(enabled = true) {
        onAction(SignUpAction.OnBackClick)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // [레이어 1] 메인 콘텐츠 (회원가입 폼 및 약관 상세)
        Crossfade(targetState = uiState.openedTermType, label = "TermDetailTransition") { termType ->
            if (termType != null) {
                TermsDetailScreen(
                    termType = termType,
                    onBackClick = { onAction(SignUpAction.CloseTermDetail) }
                )
            } else {
                SignUpFormScreen(
                    uiState,
                    onAction,
                    onBackClick = {onAction(SignUpAction.OnBackClick)})
            }
        }

        // [레이어 2] 주소 검색 Overlay (★ 여기가 추가되어야 합니다!)
        if (uiState.isStoreSearchVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f) // zIndex를 주어 가장 위에 표시
                    .background(White)
            ) {
                StoreSearchScreen(
                    uiState = uiState,
                    onAction = onAction
                )
            }
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

            AnimatedContent(
                targetState = uiState.currentStep,
                label = "SignUpStepAnimation",
                transitionSpec = {
                    // Enum의 순서(ordinal)를 비교하여 방향 결정
                    if (targetState.ordinal > initialState.ordinal) {
                        // [다음 단계로 이동]: 새 화면이 오른쪽에서 들어오고(SlideIn), 옛날 화면은 왼쪽으로 나감(SlideOut)
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut())
                    } else {
                        // [이전 단계로 이동]: 새 화면이 왼쪽에서 들어오고, 옛날 화면은 오른쪽으로 나감
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut())
                    }
                }
            ) { targetStep ->
                // 애니메이션 안에서 렌더링될 화면들
                when (targetStep) {
                    SignUpStep.STEP_1_BASIC -> Step1BasicScreen(uiState, onAction)
                    SignUpStep.STEP_2_BUSINESS -> Step2BusinessScreen(uiState, onAction)
                    SignUpStep.STEP_3_STORE -> Step3StoreScreen(uiState, onAction)
                    SignUpStep.STEP_4_OPERATION -> Step4OperatingScreen(uiState, onAction)
                    SignUpStep.STEP_5_PHOTO -> Step5PhotoScreen(uiState, onAction)
                    SignUpStep.STEP_6_COMPLETE -> Step6CompleteScreen()
                    else -> Text("준비 중")
                }
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
                    text = if(uiState.currentStep == SignUpStep.STEP_6_COMPLETE) "로그인"
                    else if(uiState.currentStep == SignUpStep.STEP_5_PHOTO) "가입하기"
                    else "다음",
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

@Preview(showBackground = true, name = "Step 3 UI")
@Composable
fun PreviewOwnerSignUpStep3Content() {
    SeatNowTheme {
        OwnerSignUpContent(
            uiState = OwnerSignUpUiState(
                currentStep = SignUpStep.STEP_3_STORE
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Step 4 UI")
@Composable
fun PreviewOwnerSignUpStep4Content() {
    SeatNowTheme {
        OwnerSignUpContent(
            uiState = OwnerSignUpUiState(
                currentStep = SignUpStep.STEP_4_OPERATION
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Step 5 UI")
@Composable
fun PreviewOwnerSignUpStep5Content() {
    SeatNowTheme {
        OwnerSignUpContent(
            uiState = OwnerSignUpUiState(
                currentStep = SignUpStep.STEP_5_PHOTO
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}
