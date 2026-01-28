package com.gmg.seatnow.presentation.owner.store.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
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
import com.gmg.seatnow.presentation.theme.SubPaleGray
import com.gmg.seatnow.presentation.theme.White
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MyPageScreen(
    viewModel: MyPageViewModel = hiltViewModel(),
    onNavigateToAccountInfo: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToEditAccount: () -> Unit,
    onNavigateToEditSeatConfig: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Event 수집 (Side Effects)
    LaunchedEffect(true) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is MyPageViewModel.MyPageEvent.NavigateToAccountInfo -> onNavigateToAccountInfo()
                is MyPageViewModel.MyPageEvent.NavigateToLogin -> onNavigateToLogin()
                is MyPageViewModel.MyPageEvent.NavigateToEditAccount -> onNavigateToEditAccount()
                is MyPageViewModel.MyPageEvent.NavigateToEditSeatConfig -> onNavigateToEditSeatConfig()
                is MyPageViewModel.MyPageEvent.NavigateToCheckPassword -> {}
                is MyPageViewModel.MyPageEvent.NavigateToChangePassword -> {}
            }
        }
    }

    // 순수 UI 컴포넌트 호출
    MyPageContent(
        isLoading = uiState.isLoading,
        onAccountInfoClick = { viewModel.onAction(MyPageAction.OnAccountInfoClick) },
        onEditSeatConfigClick = { viewModel.onAction(MyPageAction.OnEditSeatConfigClick) },
        onEditAccountInfoClick = { viewModel.onAction(MyPageAction.OnEditAccountInfoClick) }
    )
}

// 🟢 Stateless UI Component (Preview 가능)
@Composable
fun MyPageContent(
    isLoading: Boolean,
    onAccountInfoClick: () -> Unit,
    onEditSeatConfigClick: () -> Unit,
    onEditAccountInfoClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                "마이페이지",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 32.dp, top = 4.dp)
            )

            // 1. 계정 섹션
            SectionHeader("계정")

            SeatNowMenuItem(
                text = "계정 정보 수정",
                onClick = onAccountInfoClick
            )

            // 구분선이나 추가 메뉴가 필요하면 여기에 배치
            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = SubPaleGray, thickness = 1.dp)

            Spacer(modifier = Modifier.height(18.dp))

            SectionHeader("운영 설정")

            SeatNowMenuItem(
                text = "가게 정보 수정",
                onClick = onAccountInfoClick
            )

            SeatNowMenuItem(
                text = "좌석 정보 구성 수정",
                onClick = onAccountInfoClick
            )



        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyLarge,
        color = SubGray,
    )
}

// 🖼️ Preview
@Preview(showBackground = true)
@Composable
fun MyPageScreenPreview() {
    MyPageContent(
        isLoading = false,
        onAccountInfoClick = { },
        onEditSeatConfigClick = { },
        onEditAccountInfoClick = {  }
    )
}