package com.gmg.seatnow.presentation.owner.store.withdraw // ✅ 패키지 수정됨

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.theme.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun OwnerWithdrawScreen(
    viewModel: OwnerWithdrawViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(true) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is OwnerWithdrawViewModel.WithdrawEvent.NavigateToLogin -> onNavigateToLogin()
                is OwnerWithdrawViewModel.WithdrawEvent.PopBackStack -> onBackClick()
            }
        }
    }

    Scaffold(
        topBar = {
            SeatNowTopAppBar(
                title = "회원 탈퇴",
                onBackClick = { viewModel.onAction(WithdrawAction.OnBackClick) }
            )
        },
        containerColor = White,
        bottomBar = {
            WithdrawBottomBar(
                isEnabled = uiState.isConfirmed && !uiState.isLoading,
                onClick = { viewModel.onAction(WithdrawAction.OnWithdrawClick) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // 1. 헤더 (아이콘 + 텍스트)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PointRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "서비스 탈퇴 전 꼭 읽어주세요.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = SubBlack
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 안내 사항
            val points = listOf(
                "회원 탈퇴는 곧 업체의 영업 상태를 '폐업 처리' 하는 것을 의미합니다. 탈퇴 처리 즉시 해당 술집의 좌석 정보가 [폐업] 상태로 노출됩니다.",
                "탈퇴 시, 실시간 좌석 정보, 메뉴 사진, 영업 시간 등 회원이 가입 시 등록된 정보와 관련된 모든 데이터가 즉시 삭제되며 복구가 불가능합니다.",
                "부정 거래 방지 및 전자상거래법 등 관련 법령에 따라 보관이 필요한 일부 개인 정보는 해당 법정 기간 동안 안전하게 보관됩니다."
            )

            points.forEach { point ->
                Row(
                    modifier = Modifier.padding(bottom = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(text = "•", color = SubDarkGray, modifier = Modifier.padding(end = 6.dp))
                    Text(
                        text = point,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                        color = SubDarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 3. 체크박스
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onAction(WithdrawAction.OnToggleConfirm) }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (uiState.isConfirmed) PointRed else SubLightGray)
                        .then(if (!uiState.isConfirmed) Modifier.border(1.dp, SubLightGray, RoundedCornerShape(4.dp)) else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isConfirmed) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = White, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "위 유의사항을 확인했습니다.", style = MaterialTheme.typography.bodyMedium, color = SubBlack)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PointRed)
            }
        }
    }
}

@Composable
fun WithdrawBottomBar(isEnabled: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        Button(
            onClick = onClick,
            enabled = isEnabled,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PointRed,
                contentColor = White,
                disabledContainerColor = SubLightGray,
                disabledContentColor = White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "회원 탈퇴", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}