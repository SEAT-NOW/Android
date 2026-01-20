package com.gmg.seatnow.presentation.user.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.component.*
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.presentation.util.MapLogicHandler
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalNaverMapApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    initialHeadCount: Int? = null,
    onFilterCleared: () -> Unit,
    viewModel: UserHomeViewModel = hiltViewModel()
) {
    var currentUserLocation by remember { mutableStateOf<LatLng?>(null) }

    var showPermissionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 지도 상태
    val cameraPositionState = rememberCameraPositionState()
    val locationSource = rememberFusedLocationSource()
    var trackingMode by remember { mutableStateOf(LocationTrackingMode.None) }

    // 데이터 상태
    val storeList by viewModel.storeList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val activeFilter by viewModel.activeHeadCount.collectAsState()

    // Peek 높이 상태 (기본 50dp)
    var targetPeekHeight by remember { mutableStateOf(50.dp) }
    val animatedPeekHeight by animateDpAsState(
        targetValue = targetPeekHeight,
        label = "PeekHeightAnimation",
        animationSpec = tween(durationMillis = 300)
    )

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    // 반응 속도 해결 (targetValue 감시)
    val isSheetExpanded = scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded

    fun getCurrentRadius(): Double {
        val bounds = cameraPositionState.contentBounds
        return if (bounds != null) {
            val center = cameraPositionState.position.target
            val northEast = bounds.northEast
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                center.latitude, center.longitude,
                northEast.latitude, northEast.longitude,
                results
            )
            results[0] / 1000.0
        } else {
            2.0
        }
    }

    fun refreshCurrentLocation() {
        MapLogicHandler.moveCameraToCurrentLocation(
            context = context,
            cameraPositionState = cameraPositionState,
            coroutineScope = coroutineScope,
            onLocationFound = { lat, lng ->
                // ★ 위치를 찾으면 상태 변수에 저장!
                currentUserLocation = LatLng(lat, lng)

                // 검색 실행 (내 위치 정보 포함)
                viewModel.fetchStoresInCurrentMap(
                    lat = lat, // 여기선 중심점 = 내 위치
                    lng = lng,
                    radius = getCurrentRadius(),
                    userLat = lat, // 내 위치
                    userLng = lng
                )
                trackingMode = LocationTrackingMode.Follow
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            refreshCurrentLocation()
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) refreshCurrentLocation() else showPermissionDialog = true
    }

    LaunchedEffect(initialHeadCount) {
        if (initialHeadCount != null) {
            viewModel.setHeadCountFilter(initialHeadCount)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                refreshCurrentLocation()
            }
        }
    }

    // ★ [수정됨] 검색(로딩)이 시작되면 결과 유무와 상관없이 시트를 260dp로 올림
    // 기존 코드: storeList.isNotEmpty()일 때만 올리고 else면 내렸음 -> 이게 문제였음
    LaunchedEffect(isLoading) {
        if (isLoading) {
            targetPeekHeight = 260.dp
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContainerColor = White,
        sheetContentColor = SubBlack,
        sheetTonalElevation = 0.dp,
        sheetShadowElevation = 10.dp,
        sheetPeekHeight = animatedPeekHeight,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),

        // 시트 자체 스와이프 차단 (핸들바로만 제어)
        sheetSwipeEnabled = false,

        // 핸들바 수동 제어
        sheetDragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color.Transparent)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { change, dragAmount ->
                            change.consume()
                            val threshold = 5f
                            if (dragAmount < -threshold) {
                                // ▲ 위로 드래그 -> 펼치기
                                if (scaffoldState.bottomSheetState.currentValue != SheetValue.Expanded) {
                                    coroutineScope.launch { scaffoldState.bottomSheetState.expand() }
                                }
                            } else if (dragAmount > threshold) {
                                // ▼ 아래로 드래그 -> 접기
                                if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                    coroutineScope.launch { scaffoldState.bottomSheetState.partialExpand() }
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(SubLightGray, RoundedCornerShape(2.dp))
                )
            }
        },
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .background(White)
            ) {
                if (storeList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isLoading) "검색 중입니다..." else "이 지역에 등록된 술집이 없습니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SubGray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        itemsIndexed(storeList) { index, store ->
                            StoreListItem(
                                index = index + 1,
                                store = store,
                                onItemClick = {}
                            )
                            HorizontalDivider(color = SubPaleGray, thickness = 1.dp)
                        }
                    }
                }
            }
        },
        containerColor = White
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. 지도 컨텐츠
            UserMapContent(
                cameraPositionState = cameraPositionState,
                locationSource = locationSource,
                storeList = storeList,
                trackingMode = trackingMode,
                isLoading = false,
                onSearchHereClick = { },
                onCurrentLocationClick = { refreshCurrentLocation() },
                onMapGestured = {
                    // 지도 만지면 접기
                    if (targetPeekHeight > 50.dp) {
                        trackingMode = LocationTrackingMode.NoFollow
                        targetPeekHeight = 50.dp
                        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                            coroutineScope.launch { scaffoldState.bottomSheetState.partialExpand() }
                        }
                    } else {
                        trackingMode = LocationTrackingMode.NoFollow
                    }
                }
            )

            // Dim 처리
            if (isSheetExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f))
                )
            }

            // 2. 상단 UI 오버레이
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 0.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HomeSearchBar(
                    activeHeadCount = activeFilter,
                    onClearFilter = {
                        viewModel.clearHeadCountFilter()
                        onFilterCleared()
                        val target = cameraPositionState.position.target
                        viewModel.fetchStoresInCurrentMap(
                            target.latitude,
                            target.longitude,
                            getCurrentRadius()
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 버튼 표시
                if (!isSheetExpanded) {
                    SearchHereButton(
                        isLoading = isLoading,
                        onClick = {
                            val center = cameraPositionState.position.target // 지도의 중심 (검색 기준)

                            viewModel.fetchStoresInCurrentMap(
                                lat = center.latitude,
                                lng = center.longitude,
                                radius = getCurrentRadius(),
                                userLat = currentUserLocation?.latitude, // ★ 내 실제 위치 (거리 계산용)
                                userLng = currentUserLocation?.longitude // GPS가 꺼져있으면 null이 들어감 -> "0.0km" 표시됨
                            )
                        }
                    )
                }
            }

            // 3. 현재 위치 버튼
            if (!isSheetExpanded) {
                CurrentLocationButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 80.dp),
                    isSelected = trackingMode == LocationTrackingMode.Follow,
                    onClick = { refreshCurrentLocation() }
                )
            }

            if (showPermissionDialog) {
                LocationPermissionDialog(
                    onDismiss = { showPermissionDialog = false },
                    onConfirm = {
                        showPermissionDialog = false
                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                    }
                )
            }
        }
    }
}

@Composable
fun LocationPermissionDialog(
    onDismiss: () -> Unit, // '다음에 하기' 클릭 시
    onConfirm: () -> Unit  // '권한 허용' 클릭 시
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        tonalElevation = 0.dp,
        title = {
            Text(
                text = "위치 정보 이용 안내",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SubBlack
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 구분선
                HorizontalDivider(color = SubPaleGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // 안내 문구
                Text(
                    text = "내 주변의 가게와 실시간 빈 좌석 정보를\n찾기 위해 위치 권한 허용이 필요합니다.\n\n앱 설정에서 위치 권한을 허용해 주세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SubGray,
                    textAlign = TextAlign.Start, // 왼쪽 정렬 혹은 Center
                    lineHeight = 22.sp
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // 1. 다음에 하기 (선택 사항)
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "다음에 하기",
                        style = MaterialTheme.typography.labelLarge,
                        color = SubGray
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // 2. 권한 허용 (메인 액션)
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = PointRed),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "권한 허용",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = White
                    )
                }
            }
        }
    )
}