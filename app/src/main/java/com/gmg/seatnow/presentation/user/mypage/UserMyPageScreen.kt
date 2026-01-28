package com.gmg.seatnow.presentation.user.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.component.SeatNowMenuItem
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.White
import kotlinx.coroutines.flow.collectLatest

@Composable
fun UserMyPageScreen(
    viewModel: UserMyPageViewModel = hiltViewModel(),
    onNavigateToAccountInfo: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Event 수집 (Side Effects)
    LaunchedEffect(true) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is UserMyPageViewModel.UserMyPageEvent.NavigateToAccountInfo -> onNavigateToAccountInfo()
                is UserMyPageViewModel.UserMyPageEvent.NavigateToLogin -> onNavigateToLogin()
                // Withdraw는 AccountInfoScreen에서 처리하므로 여기서 라우팅하지 않음
                else -> {}
            }
        }
    }

    // 순수 UI 컴포넌트 호출
    UserMyPageContent(
        isLoading = uiState.isLoading,
        onAccountInfoClick = { viewModel.onAction(UserMyPageAction.OnAccountInfoClick) }
    )
}

@Composable
fun UserMyPageContent(
    isLoading: Boolean,
    onAccountInfoClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().background(White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                "마이페이지",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 32.dp, top = 0.dp)
            )

            // 계정 섹션 헤더
            Text("계정", style = MaterialTheme.typography.bodyLarge, color = SubGray, modifier = Modifier.padding(bottom = 8.dp))

            // 계정 정보 조회 메뉴
            SeatNowMenuItem(text = "계정 정보 조회", onClick = onAccountInfoClick)
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserMyPageScreenPreview() {
    UserMyPageContent(isLoading = false, onAccountInfoClick = {})
}