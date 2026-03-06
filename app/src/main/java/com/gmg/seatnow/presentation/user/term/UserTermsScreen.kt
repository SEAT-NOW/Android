package com.gmg.seatnow.presentation.user.term

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.component.TermItem
import com.gmg.seatnow.presentation.theme.*

@Composable
fun UserTermsScreen(
    viewModel: UserTermsViewModel = hiltViewModel(),
    onNavigateToBack: () -> Unit,
    onNavigateToMain: () -> Unit,
    isGuest: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(true) {
        viewModel.event.collect { event ->
            when (event) {
                is UserTermsEvent.NavigateToMain -> {
                    onNavigateToMain()
                }
            }
        }
    }

    // 상세 화면이 열려있으면 뒤로가기 시 상세 화면 닫기
    BackHandler(enabled = uiState.openedTermType != null) {
        viewModel.onAction(UserTermsAction.OnCloseDetail)
    }

    // 화면 전환 애니메이션 (리스트 <-> 상세)
    Crossfade(targetState = uiState.openedTermType, label = "UserTermsTransition") { termType ->
        if (termType != null) {
            // 상세 화면
            UserTermsDetailScreen(
                termType = termType,
                onBackClick = { viewModel.onAction(UserTermsAction.OnCloseDetail) }
            )
        } else {
            // 약관 리스트 화면
            UserTermsListContent(
                uiState = uiState,
                onToggleAll = { viewModel.onAction(UserTermsAction.OnToggleAll(it)) },
                onToggleTerm = { viewModel.onAction(UserTermsAction.OnToggleTerm(it)) },
                onOpenDetail = { viewModel.onAction(UserTermsAction.OnOpenDetail(it)) },
                onNavigateToBack = onNavigateToBack,
                onSaveAndNavigate = {
                    viewModel.onAction(UserTermsAction.OnSaveTermsAgreement(isGuest))
                }
            )
        }
    }
}

@Composable
fun UserTermsListContent(
    uiState: UserTermsUiState,
    onToggleAll: (Boolean) -> Unit,
    onToggleTerm: (UserTermType) -> Unit,
    onOpenDetail: (UserTermType) -> Unit,
    onNavigateToBack: () -> Unit,
    onSaveAndNavigate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .systemBarsPadding()
    ) {
        // 1. 상단 바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "약관 동의",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                tint = SubBlack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { onNavigateToBack() }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. 전체 동의
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleAll(!uiState.isAllChecked) } // 전체 영역 클릭 가능
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = uiState.isAllChecked,
                onCheckedChange = { onToggleAll(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = PointRed,
                    uncheckedColor = SubLightGray,
                    checkmarkColor = White
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "서비스 이용약관 모두 동의",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = SubBlack
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp),
            thickness = 1.dp,
            color = SubPaleGray
        )

        // 3. 약관 리스트
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            TermItem(
                title = UserTermType.AGE.title,
                isChecked = uiState.isAgeChecked,
                showArrow = false,
                onToggle = { onToggleTerm(UserTermType.AGE) }
            )
            TermItem(
                title = UserTermType.SERVICE.title,
                isChecked = uiState.isServiceChecked,
                showArrow = true,
                onToggle = { onToggleTerm(UserTermType.SERVICE) },
                onDetailClick = { onOpenDetail(UserTermType.SERVICE) } // 화살표 클릭 시 상세 이동
            )
            TermItem(
                title = UserTermType.PRIVACY_COLLECT.title,
                isChecked = uiState.isPrivacyCollectChecked,
                showArrow = true,
                onToggle = { onToggleTerm(UserTermType.PRIVACY_COLLECT) },
                onDetailClick = { onOpenDetail(UserTermType.PRIVACY_COLLECT) }
            )
            TermItem(
                title = UserTermType.PRIVACY_PROVIDE.title,
                isChecked = uiState.isPrivacyProvideChecked,
                showArrow = true,
                onToggle = { onToggleTerm(UserTermType.PRIVACY_PROVIDE) },
                onDetailClick = { onOpenDetail(UserTermType.PRIVACY_PROVIDE) }
            )
            TermItem(
                title = UserTermType.LOCATION.title,
                isChecked = uiState.isLocationChecked,
                showArrow = true,
                onToggle = { onToggleTerm(UserTermType.LOCATION) },
                onDetailClick = { onOpenDetail(UserTermType.LOCATION) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 4. 하단 버튼
        Button(
            onClick = onSaveAndNavigate,
            enabled = uiState.isNextEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
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
                text = "동의하고 시작하기",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

// 상세 약관 화면
@Composable
fun UserTermsDetailScreen(
    termType: UserTermType,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            SeatNowTopAppBar(
                title = termType.title.replace("[필수]", "").trim(),
                onBackClick = onBackClick,
                topMargin = 15.dp
            )
        },
        containerColor = White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            val content = when(termType) {
                UserTermType.SERVICE -> getServiceTermsMock()
                UserTermType.PRIVACY_COLLECT -> getPrivacyCollectMock()
                UserTermType.PRIVACY_PROVIDE -> getPrivacyProvideMock()
                UserTermType.LOCATION -> getLocationTermsMock() // 위치기반 추가
                else -> "내용이 없습니다."
            }

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = SubBlack,
                lineHeight = 24.sp
            )
        }
    }
}

