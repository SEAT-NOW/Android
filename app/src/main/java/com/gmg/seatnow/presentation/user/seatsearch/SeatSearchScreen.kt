package com.gmg.seatnow.presentation.user.seatsearch

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.component.UserMapContent
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SubBlack
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.White
import com.gmg.seatnow.presentation.util.MapLogicHandler
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberFusedLocationSource
import com.gmg.seatnow.R
import com.naver.maps.map.compose.ExperimentalNaverMapApi

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun SeatSearchScreen(
    resetKey: Long = 0L,
    viewModel: SeatSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val cameraPositionState = rememberCameraPositionState()
    val locationSource = rememberFusedLocationSource()
    var trackingMode by remember { mutableStateOf(LocationTrackingMode.None) }

    LaunchedEffect(resetKey) {
        if (resetKey != 0L) {
            viewModel.reset()
        }
    }

    // 뒤로가기 핸들링
    BackHandler(enabled = uiState.step == SeatSearchViewModel.SearchStep.MAP) {
        viewModel.goBackToInput()
    }

    // 검색 실행 로직
    fun executeSearch() {
        focusManager.clearFocus()
        MapLogicHandler.moveCameraToCurrentLocation(
            context = context,
            cameraPositionState = cameraPositionState,
            coroutineScope = coroutineScope,
            onLocationFound = { lat, lng ->
                viewModel.searchStores(lat, lng)
                trackingMode = LocationTrackingMode.Follow
            }
        )
    }

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
        when (uiState.step) {
            // [Step 1] 인원수 입력 화면 (수정됨)
            SeatSearchViewModel.SearchStep.INPUT -> {
                SearchInputContent(
                    headCount = uiState.headCount,
                    onCountChange = viewModel::updateHeadCount,
                    onAdjust = viewModel::adjustHeadCount, // +1, +5 등을 위해 통합
                    onFocusClear = viewModel::finalizeHeadCount,
                    onSearchClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            executeSearch()
                        } else {
                            viewModel.searchStores(37.5665, 126.9780)
                        }
                    }
                )
            }

            // [Step 2] 지도 결과 화면
            SeatSearchViewModel.SearchStep.MAP -> {
                UserMapContent(
                    cameraPositionState = cameraPositionState,
                    locationSource = locationSource,
                    storeList = uiState.filteredStoreList,
                    trackingMode = trackingMode,
                    isLoading = uiState.isLoading,
                    onSearchHereClick = {
                        val center = cameraPositionState.position.target
                        viewModel.searchStores(center.latitude, center.longitude)
                    },
                    onCurrentLocationClick = {
                        MapLogicHandler.moveCameraToCurrentLocation(
                            context = context,
                            cameraPositionState = cameraPositionState,
                            coroutineScope = coroutineScope,
                            onLocationFound = { _, _ -> trackingMode = LocationTrackingMode.Follow }
                        )
                    },
                    onMapGestured = { trackingMode = LocationTrackingMode.NoFollow }
                )
            }
        }
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
    // 포커스 상태 감지
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current

    // [수정] 배경색 및 테두리: 포커스 시 White + Border, 아닐 시 LightGray
    val circleBackgroundColor = if (isFocused) White else Color(0xFFF6F6F6)
    val circleBorderColor = if (isFocused) SubGray else Color.Transparent
    val circleBorderWidth = if (isFocused) 2.dp else 0.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .imePadding() // ★ 키패드 올라오면 전체 레이아웃을 밀어올림
    ) {
        // 중앙 컨텐츠 (타이틀, 숫자 조절, 버튼)
        // 키패드가 올라와서 공간이 줄어들면 Alignment.Center에 의해 자동으로 위쪽 공간의 중앙으로 이동함
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-20).dp), // 시각적 중심 보정
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "몇 명이신가요?",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = SubBlack
            )

            Spacer(modifier = Modifier.height(30.dp)) // 간격 조정

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Minus
                IconButton(
                    onClick = { onAdjust(-1) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_minus),
                        contentDescription = "감소",
                        tint = SubBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // ★ Center Circle Input
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(circleBackgroundColor) // 상태에 따른 배경색
                        .border(circleBorderWidth, circleBorderColor, CircleShape), // 상태에 따른 테두리
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = headCount,
                        onValueChange = onCountChange,
                        interactionSource = interactionSource, // 포커스 감지 연결
                        textStyle = TextStyle(
                            color = PointRed,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        // [수정] 키보드 옵션: 숫자만, 완료 버튼 활성화
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        // [수정] 완료 버튼 클릭 시 포커스 해제 -> onFocusChanged 트리거
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(PointRed),
                        modifier = Modifier
                            .width(100.dp)
                            // [핵심] 포커스 변경 감지 -> 포커스 잃으면 빈 값 체크 실행
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    onFocusClear()
                                }
                            }
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Plus
                IconButton(
                    onClick = { onAdjust(1) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = "증가",
                        tint = SubBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 빠른 추가 버튼들
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
        }

        // 하단 버튼 (키패드가 올라오면 그 바로 위에 위치)
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