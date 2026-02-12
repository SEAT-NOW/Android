package com.gmg.seatnow.presentation.user.seatsearch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.R
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SubBlack
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.White

@Composable
fun SeatSearchScreen(
    onSearchConfirmed: (Int) -> Unit,
    viewModel: SeatSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // 배경 클릭 시 포커스 해제
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() }
    ) {
        SearchInputContent(
            headCount = uiState.headCount,
            onCountChange = viewModel::updateHeadCount,
            onAdjust = viewModel::adjustHeadCount,
            onFocusClear = viewModel::finalizeHeadCount,
            onSearchClick = {
                val count = uiState.headCount.toIntOrNull() ?: 0
                if (count > 0) {
                    onSearchConfirmed(count)
                }
            }
        )
    }
}

@Composable
fun SearchInputContent(
    headCount: String,
    onCountChange: (String) -> Unit,
    onAdjust: (Int) -> Unit,
    onFocusClear: () -> Unit,
    onSearchClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current

    val circleBackgroundColor = if (isFocused) White else Color(0xFFF6F6F6)
    val circleBorderColor = if (isFocused) SubGray else Color.Transparent
    val circleBorderWidth = if (isFocused) 2.dp else 0.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // ★ [추가] 상단 타이틀 (KeepScreen과 동일한 위치/스타일)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(top = 20.dp) // 상단 여백
        ) {
            Text(
                text = "N명 자리 찾기",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = SubBlack,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // [중앙 컨텐츠]
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "몇 명이신가요?",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = SubBlack
            )

            Spacer(modifier = Modifier.height(30.dp))

            // ... (기존 카운터 UI 유지)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { onAdjust(-1) }, modifier = Modifier.size(48.dp)) {
                    Icon(painter = painterResource(id = R.drawable.ic_minus), contentDescription = "감소", tint = SubBlack, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(20.dp))

                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(circleBackgroundColor)
                        .border(circleBorderWidth, circleBorderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = headCount,
                        onValueChange = onCountChange,
                        interactionSource = interactionSource,
                        textStyle = TextStyle(
                            color = PointRed,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        singleLine = true,
                        cursorBrush = SolidColor(PointRed),
                        modifier = Modifier
                            .width(100.dp)
                            .onFocusChanged { if (!it.isFocused) onFocusClear() }
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                IconButton(onClick = { onAdjust(1) }, modifier = Modifier.size(48.dp)) {
                    Icon(painter = painterResource(id = R.drawable.ic_plus), contentDescription = "증가", tint = SubBlack, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(1, 5, 10).forEach { amount ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFE0E0E0))
                            .clickable { onAdjust(amount) }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ $amount",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = SubGray
                        )
                    }
                }
            }

            // 버튼과 겹치지 않게 하단 여백 추가
            Spacer(modifier = Modifier.height(80.dp))
        }

        // [하단 버튼]
        Button(
            onClick = onSearchClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PointRed,
                contentColor = White
            )
        ) {
            Text(
                text = "술집 탐색하기",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}