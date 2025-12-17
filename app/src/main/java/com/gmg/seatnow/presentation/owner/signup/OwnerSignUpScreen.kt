package com.gmg.seatnow.presentation.owner.signup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OwnerSignUpScreen(
    viewModel: OwnerSignUpViewModel = hiltViewModel(),
    onBackClick: () -> Unit, // 네비게이션 동작은 상위에서 받음
    onNavigateToHome: () -> Unit
) {
    // 1. ViewModel의 State 수집
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 2. ViewModel의 Event(화면이동 등) 수집
    LaunchedEffect(viewModel.event) {
        viewModel.event.collect { event ->
            when(event) {
                is SignUpEvent.NavigateBack -> onBackClick()
                is SignUpEvent.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    // 3. 순수 UI(Content) 호출
    OwnerSignUpContent(
        uiState = uiState,
        onAction = viewModel::onAction, // 뷰모델의 함수를 참조로 넘김
        onBackClick = onBackClick // 여기서는 단순히 뒤로가기 버튼 클릭 시 동작
    )
}