package com.gmg.seatnow.presentation.user.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
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
import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.presentation.component.*
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.presentation.util.MapLogicHandler
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
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
    var selectedStoreId by remember { mutableStateOf<Long?>(null) }

    var showPermissionDialog by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 지도 상태 (Hoisting)
    val cameraPositionState = rememberCameraPositionState()
    val locationSource = rememberFusedLocationSource()
    var trackingMode by remember { mutableStateOf(LocationTrackingMode.None) }

    // 데이터 상태
    val storeList by viewModel.storeList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val activeFilter by viewModel.activeHeadCount.collectAsState()

    // 뒤로가기 핸들러
    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        viewModel.clearSearch()
    }

    // ★ [핵심] Peek 높이 제어
    // 검색 중이거나 핀이 선택되면 시트를 내리거나 숨김 (0dp or 50dp)
    // 그 외엔 50dp, 로딩 시 260dp
    var targetPeekHeight by remember { mutableStateOf(50.dp) }

    // 로딩 및 상태에 따른 시트 높이 자동 조절
    LaunchedEffect(isLoading, isSearchActive, selectedStoreId) {
        targetPeekHeight = when {
            isSearchActive -> 0.dp // 검색 중엔 시트 숨김
            selectedStoreId != null -> 50.dp // 핀 선택 시 최소 높이
            isLoading -> 260.dp // 로딩 시 목록 보여줌
            else -> 50.dp // 기본
        }
    }

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
    val isSheetExpanded = scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded

    // 반경 계산
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

    // 현재 위치 갱신 및 검색
    fun refreshCurrentLocation() {
        MapLogicHandler.moveCameraToCurrentLocation(
            context = context,
            cameraPositionState = cameraPositionState,
            coroutineScope = coroutineScope,
            onLocationFound = { lat, lng ->
                currentUserLocation = LatLng(lat, lng)
                viewModel.fetchStoresInCurrentMap(
                    lat = lat,
                    lng = lng,
                    radius = getCurrentRadius(),
                    userLat = lat,
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

    // =================================================================
    // ★ [구조 변경] BottomSheetScaffold가 최상위를 감싸고,
    // 지도(UserMapContent)는 항상 그 안에 존재해야 함 (Recomposition 방지)
    // =================================================================
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContainerColor = White,
        sheetContentColor = SubBlack,
        sheetTonalElevation = 0.dp,
        sheetShadowElevation = 10.dp,
        sheetPeekHeight = animatedPeekHeight, // 0dp가 되면 숨겨짐
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetSwipeEnabled = !isSearchActive, // 검색 중엔 스와이프 막음
        sheetDragHandle = {
            if (!isSearchActive) { // 검색 중엔 핸들 숨김
                Box(
                    modifier = Modifier.fillMaxWidth().height(48.dp).background(Color.Transparent)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { change, dragAmount ->
                                change.consume()
                                val threshold = 5f
                                if (dragAmount < -threshold) {
                                    if (scaffoldState.bottomSheetState.currentValue != SheetValue.Expanded) {
                                        coroutineScope.launch { scaffoldState.bottomSheetState.expand() }
                                    }
                                } else if (dragAmount > threshold) {
                                    if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                        coroutineScope.launch { scaffoldState.bottomSheetState.partialExpand() }
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.width(40.dp).height(4.dp).background(SubLightGray, RoundedCornerShape(2.dp)))
                }
            }
        },
        sheetContent = {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f).background(White)
            ) {
                if (storeList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 50.dp), contentAlignment = Alignment.Center) {
                        Text(if (isLoading) "검색 중입니다..." else "이 지역에 등록된 술집이 없습니다.", style = MaterialTheme.typography.bodyMedium, color = SubGray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp)) {
                        itemsIndexed(storeList) { index, store ->
                            StoreListItem(
                                index = index + 1,
                                store = store,
                                onItemClick = {
                                    selectedStoreId = store.id
                                    // 리스트 클릭 시에도 지도 이동 + Follow 끊기 적용
                                    trackingMode = LocationTrackingMode.None
                                    coroutineScope.launch {
                                        cameraPositionState.animate(
                                            update = CameraUpdate.scrollTo(LatLng(store.latitude, store.longitude)),
                                            durationMs = 800
                                        )
                                    }
                                }
                            )
                            HorizontalDivider(color = SubPaleGray, thickness = 1.dp)
                        }
                    }
                }
            }
        },
        containerColor = White
    ) { paddingValues ->
        // ★ [핵심] 지도가 포함된 메인 컨텐츠 영역
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. 지도 (조건문 없이 항상 존재해야 함 -> 그래야 이동 애니메이션이 안 끊김)
            UserMapContent(
                cameraPositionState = cameraPositionState,
                locationSource = locationSource,
                storeList = storeList, // 검색 중엔 빈 리스트를 넣고 싶으면 if문 처리 가능하지만, 보통 유지하는게 자연스러움
                trackingMode = trackingMode,
                isLoading = false,
                selectedStoreId = selectedStoreId,
                onStoreClick = { storeId -> selectedStoreId = storeId },
                onMapClick = {
                    if (isSearchActive) {
                        isSearchActive = false
                        viewModel.clearSearch()
                    } else {
                        selectedStoreId = null
                    }
                },
                onSearchHereClick = { },
                onCurrentLocationClick = { refreshCurrentLocation() },
                onMapGestured = {
                    // 지도 움직임 감지
                    if (targetPeekHeight > 50.dp && !isSearchActive) {
                        trackingMode = LocationTrackingMode.NoFollow
                        // 시트 내리기
                        if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                            coroutineScope.launch { scaffoldState.bottomSheetState.partialExpand() }
                        }
                    } else {
                        trackingMode = LocationTrackingMode.NoFollow
                    }

                    // 핀 선택 해제 (지도 드래그 시)
                    if (selectedStoreId != null) {
                        selectedStoreId = null
                    }
                }
            )

            // Dim 처리 (시트 확장 시)
            if (isSheetExpanded && !isSearchActive) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)))
            }

            // 2. [검색 화면 오버레이] (isSearchActive일 때 지도 위에 덮어씌움)
            if (isSearchActive) {
                val center = cameraPositionState.position.target

                // 배경을 흰색으로 덮어서 지도 안보이게 처리 (원하면)
                Box(modifier = Modifier.fillMaxSize().background(White)) {
                    UserSearchScreen(
                        onBackClick = {
                            isSearchActive = false
                            viewModel.clearSearch()
                        },
                        onStoreClick = { store ->
                            // 1. 화면 전환
                            isSearchActive = false
                            viewModel.clearSearch()

                            // 2. ★ [핵심] 지도 추적 모드 해제 (이게 켜져 있으면 애니메이션 씹힘)
                            trackingMode = LocationTrackingMode.None

                            // 3. 핀 선택 상태 활성화 (Ghost Item 문제 해결을 위해 ID 먼저 세팅)
                            selectedStoreId = store.id

                            // 4. 지도 데이터 갱신 (해당 위치 중심)
                            viewModel.fetchStoresInCurrentMap(
                                lat = store.latitude,
                                lng = store.longitude,
                                radius = 2.0,
                                userLat = currentUserLocation?.latitude,
                                userLng = currentUserLocation?.longitude
                            )

                            // 5. 카메라 강제 이동
                            coroutineScope.launch {
                                // 혹시 모를 이전 애니메이션 취소
                                cameraPositionState.stop()
                                cameraPositionState.animate(
                                    update = CameraUpdate.scrollTo(LatLng(store.latitude, store.longitude)),
                                    durationMs = 1000
                                )
                            }
                        },
                        viewModel = viewModel,
                        currentLat = center.latitude,
                        currentLng = center.longitude,
                        userLat = currentUserLocation?.latitude,
                        userLng = currentUserLocation?.longitude
                    )
                }
            }
            // 3. [일반 홈 UI] (검색 중이 아닐 때만 표시)
            else {
                // 상단 UI (검색바 등)
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
                                getCurrentRadius(),
                                currentUserLocation?.latitude,
                                currentUserLocation?.longitude
                            )
                        },
                        onSearchClick = { isSearchActive = true }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isSheetExpanded && selectedStoreId == null) {
                        SearchHereButton(
                            isLoading = isLoading,
                            onClick = {
                                val center = cameraPositionState.position.target
                                viewModel.fetchStoresInCurrentMap(
                                    lat = center.latitude,
                                    lng = center.longitude,
                                    radius = getCurrentRadius(),
                                    userLat = currentUserLocation?.latitude,
                                    userLng = currentUserLocation?.longitude
                                )
                            }
                        )
                    }
                }

                // 현재 위치 버튼
                if (!isSheetExpanded && selectedStoreId == null) {
                    CurrentLocationButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 80.dp),
                        isSelected = trackingMode == LocationTrackingMode.Follow,
                        onClick = { refreshCurrentLocation() }
                    )
                }

                // 선택된 가게 상세 카드
                if (selectedStoreId != null) {
                    // 현재 리스트에 있는 녀석인지 확인, 없으면 핀만 찍히고 카드는 로딩 후 뜸
                    val selectedIndex = storeList.indexOfFirst { it.id == selectedStoreId }
                    val selectedStore = storeList.getOrNull(selectedIndex)

                    if (selectedStore != null) {
                        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 50.dp)) {
                            StoreDetailCard(
                                index = selectedIndex + 1,
                                store = selectedStore,
                                onClose = { selectedStoreId = null },
                                onItemClick = { }
                            )
                        }
                    }
                }
            }
        }
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

@Composable
fun LocationPermissionDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        tonalElevation = 0.dp,
        title = { Text("위치 정보 이용 안내", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SubBlack) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(color = SubPaleGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("내 주변의 가게와 실시간 빈 좌석 정보를\n찾기 위해 위치 권한 허용이 필요합니다.\n\n앱 설정에서 위치 권한을 허용해 주세요.", style = MaterialTheme.typography.bodyMedium, color = SubGray, textAlign = TextAlign.Start, lineHeight = 22.sp)
            }
        },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("다음에 하기", style = MaterialTheme.typography.labelLarge, color = SubGray) }
                Spacer(modifier = Modifier.width(4.dp))
                Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = PointRed), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp)) {
                    Text("권한 허용", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = White)
                }
            }
        }
    )
}