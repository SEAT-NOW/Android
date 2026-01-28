package com.gmg.seatnow.presentation.owner.store.withdraw

import android.R.id.bold
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.component.BusinessNumberVisualTransformation
import com.gmg.seatnow.presentation.component.SeatNowTextField
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

    OwnerWithdrawContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onBackClick = onBackClick
    )
}

@Composable
fun OwnerWithdrawContent(
    uiState: OwnerWithdrawViewModel.WithdrawUiState,
    onAction: (WithdrawAction) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            SeatNowTopAppBar(
                title = "회원 탈퇴(업체 삭제)",
                onBackClick = { onAction(WithdrawAction.OnBackClick) }
            )
        },
        containerColor = White,
        bottomBar = {
            WithdrawBottomBar(
                isEnabled = uiState.isButtonEnabled,
                errorMessage = uiState.errorMessage,
                onClick = { onAction(WithdrawAction.OnWithdrawClick) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // [상단] 안내사항 및 동의 체크박스
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(32.dp))

                // 헤더
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
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = SubBlack
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 안내 문구
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
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 15.sp, fontWeight = FontWeight.Bold),
                            color = SubDarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 체크박스
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAction(WithdrawAction.OnToggleConfirm) }
                        .padding(vertical = 12.dp),
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

                Spacer(modifier = Modifier.height(24.dp))
            }

            // [구분선]
            HorizontalDivider(
                thickness = 2.dp,
                color = SubPaleGray
            )

            // [하단] 입력 필드 영역
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(32.dp))

                // 사업자번호 입력
                Text(
                    text = "사업자등록번호 입력",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = SubBlack
                )
                Spacer(modifier = Modifier.height(12.dp))
                SeatNowTextField(
                    value = uiState.businessNumber,
                    onValueChange = { input ->
                        // 숫자만 입력받고, 최대 10자리까지만 허용 (자동 하이픈 로직을 위해)
                        if (input.length <= 10 && input.all { it.isDigit() }) {
                            onAction(WithdrawAction.OnBusinessNumberChange(input))
                        }
                    },
                    placeholder = "사업자등록번호 (숫자만 입력)",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                    visualTransformation = BusinessNumberVisualTransformation() // ★ 사용자님 로직 적용
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 비밀번호 입력
                Text(
                    text = "비밀번호 입력",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = SubBlack
                )
                Spacer(modifier = Modifier.height(12.dp))
                SeatNowTextField(
                    value = uiState.password,
                    onValueChange = { onAction(WithdrawAction.OnPasswordChange(it)) },
                    placeholder = "비밀번호",
                    isPassword = true,
                    imeAction = ImeAction.Done
                )

                // 하단 버튼에 가려지지 않게 여백 추가
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PointRed)
            }
        }
    }
}

@Composable
fun WithdrawBottomBar(
    isEnabled: Boolean,
    errorMessage: String?,
    onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .imePadding() // 키보드 패딩 적용
    ) {
        // [에러 메시지 영역]
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Red, fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally) // 가운데 정렬 (선택사항)
                    .padding(bottom = 6.dp)
            )
        } else {
            // 에러 없을 때 높이 맞춤용 Spacer (36dp는 에러 메시지가 차지할 대략적인 공간)
            // 에러 메시지 폰트 사이즈 + 패딩 등을 고려하여 높이 설정
            Spacer(modifier = Modifier.height(24.dp)) // 요청하신 예시는 36dp였으나, 24dp 정도가 적당해 보여 조정 가능
        }

        // [버튼]
        Button(
            onClick = onClick,
            enabled = isEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
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

// ================= PREVIEW =================

@Preview(showBackground = true, name = "Default State")
@Composable
fun PreviewOwnerWithdrawScreen_Default() {
    SeatNowTheme {
        OwnerWithdrawContent(
            uiState = OwnerWithdrawViewModel.WithdrawUiState(
                isConfirmed = false,
                businessNumber = "",
                password = "",
                isLoading = false
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Filled & Enabled State")
@Composable
fun PreviewOwnerWithdrawScreen_Enabled() {
    SeatNowTheme {
        OwnerWithdrawContent(
            uiState = OwnerWithdrawViewModel.WithdrawUiState(
                isConfirmed = true,
                businessNumber = "1234567890",
                password = "password123!",
                isLoading = false
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}