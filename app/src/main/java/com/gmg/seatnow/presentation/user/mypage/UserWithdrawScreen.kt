package com.gmg.seatnow.presentation.user.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.theme.*

@Composable
fun UserWithdrawScreen(
    viewModel: UserWithdrawViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(true) {
        viewModel.event.collect { event ->
            when (event) {
                is UserWithdrawViewModel.UserWithdrawEvent.NavigateToLogin -> onNavigateToLogin()
                is UserWithdrawViewModel.UserWithdrawEvent.PopBackStack -> onBackClick()
            }
        }
    }

    Scaffold(
        topBar = { SeatNowTopAppBar(title = "회원 탈퇴", onBackClick = viewModel::onBackClick) },
        containerColor = White,
        bottomBar = {
            Button(
                onClick = viewModel::onWithdrawClick,
                enabled = uiState.isConfirmed && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().padding(24.dp).height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PointRed, disabledContainerColor = SubLightGray),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "회원 탈퇴", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(32.dp))

            // 1. 유의사항 헤더
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = PointRed, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("서비스 탈퇴 전 꼭 읽어주세요.", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 유저용 안내 문구 (기존 사장님 코드 변형)
            val points = listOf(
                "탈퇴 시, 킵 술집 목록과 저장된 회원 정보와 관련된 모든    데이터가 즉시 삭제되며 복구가 불가능합니다.",
                "부정 이용 방지 및 전자상거래법 등 관련 법령에 따라 보관이 필요한 정보는 해당 기간 동안 안전하게 보관됩니다."
            )
            points.forEach { point ->
                Row(modifier = Modifier.padding(bottom = 12.dp), verticalAlignment = Alignment.Top) {
                    Text("•", color = SubDarkGray, modifier = Modifier.padding(end = 6.dp))
                    Text(point, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 15.sp), color = SubDarkGray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. 동의 체크박스 (입력란 없음)
            Row(
                modifier = Modifier.fillMaxWidth().clickable { viewModel.onToggleConfirm() }.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(20.dp).clip(RoundedCornerShape(4.dp))
                        .background(if (uiState.isConfirmed) PointRed else SubLightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isConfirmed) Icon(Icons.Default.Check, contentDescription = null, tint = White, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("위 유의사항을 확인했습니다.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun UserWithdrawContent(
    isConfirmed: Boolean,
    isLoading: Boolean,
    onToggleConfirm: () -> Unit,
    onWithdrawClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = { SeatNowTopAppBar(title = "회원 탈퇴", onBackClick = onBackClick) },
        containerColor = White,
        bottomBar = {
            Button(
                onClick = onWithdrawClick,
                enabled = isConfirmed && !isLoading, // 동의해야 버튼 활성화
                modifier = Modifier.fillMaxWidth().padding(24.dp).height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PointRed, disabledContainerColor = SubLightGray),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "회원 탈퇴", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(32.dp))

            // 1. 유의사항 헤더
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = PointRed, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("서비스 탈퇴 전 꼭 읽어주세요.", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 안내 문구
            val points = listOf(
                "탈퇴 시, 회원님의 개인정보 및 서비스 이용 기록이 즉시 파기되며 복구할 수 없습니다.",
                "부정 이용 방지 및 전자상거래법 등 관련 법령에 따라 보관이 필요한 정보는 해당 기간 동안 안전하게 보관됩니다."
            )
            points.forEach { point ->
                Row(modifier = Modifier.padding(bottom = 12.dp), verticalAlignment = Alignment.Top) {
                    Text("•", color = SubDarkGray, modifier = Modifier.padding(end = 6.dp))
                    Text(point, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 15.sp), color = SubDarkGray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. 동의 체크박스
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onToggleConfirm() }.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(20.dp).clip(RoundedCornerShape(4.dp))
                        .background(if (isConfirmed) PointRed else SubLightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (isConfirmed) Icon(Icons.Default.Check, contentDescription = null, tint = White, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("위 유의사항을 확인했습니다.", style = MaterialTheme.typography.bodyMedium)
            }
        }

        // 로딩 바
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PointRed)
            }
        }
    }
}

// ================= PREVIEW =================

@Preview(showBackground = true, name = "기본 상태 (비활성화)")
@Composable
fun UserWithdrawScreenPreview_Default() {
    SeatNowTheme {
        UserWithdrawContent(
            isConfirmed = false,
            isLoading = false,
            onToggleConfirm = {},
            onWithdrawClick = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "동의 체크 상태 (활성화)")
@Composable
fun UserWithdrawScreenPreview_Confirmed() {
    SeatNowTheme {
        UserWithdrawContent(
            isConfirmed = true,
            isLoading = false,
            onToggleConfirm = {},
            onWithdrawClick = {},
            onBackClick = {}
        )
    }
}