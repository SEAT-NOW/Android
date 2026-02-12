package com.gmg.seatnow.presentation.user.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.presentation.component.*
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.presentation.util.IntentUtil
import com.gmg.seatnow.presentation.util.MapLogicHandler
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.*
import kotlinx.coroutines.launch

enum class BottomSheetStep {
    COLLAPSED, // 40dp (핸들바만)
    HALF,      // 260dp (기본 리스트)
    FULL       // 꽉 채움
}

@OptIn(ExperimentalNaverMapApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    initialHeadCount: Int? = null,
    onFilterCleared: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: UserHomeViewModel = hiltViewModel()
) {
    var currentUserLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedStoreId by remember { mutableStateOf<Long?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }

    var sheetStep by remember { mutableStateOf(BottomSheetStep.HALF) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var savedCameraLat by rememberSaveable { mutableStateOf<Double?>(null) }
    var savedCameraLng by rememberSaveable { mutableStateOf<Double?>(null) }
    var savedZoom by rememberSaveable { mutableStateOf(15.0) }

    val cameraPositionState = rememberCameraPositionState {
        if (savedCameraLat != null && savedCameraLng != null) {
            position = com.naver.maps.map.CameraPosition(
                LatLng(savedCameraLat!!, savedCameraLng!!),
                savedZoom
            )
        }
    }
    val locationSource = rememberFusedLocationSource()
    var trackingMode by remember { mutableStateOf(LocationTrackingMode.None) }

    val storeList by viewModel.storeList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val activeFilter by viewModel.activeHeadCount.collectAsState()

    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        viewModel.clearSearch()
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipHiddenState = true
        )
    )

    val targetPeekHeight = when {
        isSearchActive -> 0.dp
        sheetStep == BottomSheetStep.COLLAPSED -> 40.dp
        else -> 260.dp // HALF 이거나 FULL 일 때 기본 베이스는 260dp
    }

    val animatedPeekHeight by animateDpAsState(
        targetValue = targetPeekHeight,
        animationSpec = if (isSearchActive) snap() else tween(durationMillis = 200),
        label = "PeekHeightAnimation"
    )

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
                trackingMode = LocationTrackingMode.Follow

                currentUserLocation = LatLng(lat, lng)
                viewModel.fetchStoresInCurrentMap(
                    lat = lat,
                    lng = lng,
                    radius = getCurrentRadius(),
                    userLat = lat,
                    userLng = lng
                )
                sheetStep = BottomSheetStep.HALF
                coroutineScope.launch { scaffoldState.bottomSheetState.partialExpand() }
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
        if (storeList.isNotEmpty() && savedCameraLat != null) {
            return@LaunchedEffect
        }

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

    LaunchedEffect(cameraPositionState.position) {
        savedCameraLat = cameraPositionState.position.target.latitude
        savedCameraLng = cameraPositionState.position.target.longitude
        savedZoom = cameraPositionState.position.zoom
    }

    val isSheetExpanded = scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded


    LaunchedEffect(selectedStoreId) {
        if (selectedStoreId != null) {
            if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                scaffoldState.bottomSheetState.partialExpand()
            }
        }
    }

    LaunchedEffect(scaffoldState.bottomSheetState.targetValue) {
        val target = scaffoldState.bottomSheetState.targetValue
        if (target == SheetValue.Expanded) {
            // 사용자가 리스트를 위로 밀어서 꽉 차게 만들었을 때
            sheetStep = BottomSheetStep.FULL
        } else if (target == SheetValue.PartiallyExpanded && sheetStep == BottomSheetStep.FULL) {
            // FULL에서 아래로 내려왔을 때 HALF로 고정 (COLLAPSED로 튕김 방지)
            sheetStep = BottomSheetStep.HALF
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
        sheetSwipeEnabled = false,
        sheetDragHandle = {
            if (!isSearchActive) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(48.dp).background(Color.White)
                        .pointerInput(Unit) { // 제스처 감지기 유지
                            var dragAccumulator = 0f
                            detectVerticalDragGestures(
                                onDragStart = { dragAccumulator = 0f },
                                onDragEnd = {
                                    if (dragAccumulator < -20f) { // 위로 올림
                                        selectedStoreId = null
                                        when (sheetStep) {
                                            BottomSheetStep.COLLAPSED -> sheetStep = BottomSheetStep.HALF
                                            BottomSheetStep.HALF -> {
                                                sheetStep = BottomSheetStep.FULL
                                                coroutineScope.launch { scaffoldState.bottomSheetState.expand() }
                                            }
                                            BottomSheetStep.FULL -> {}
                                        }
                                    } else if (dragAccumulator > 20f) { // 아래로 내림
                                        when (sheetStep) {
                                            BottomSheetStep.FULL -> {
                                                // HALF로 상태만 변경. 애니메이션은 Compose 기본 물리 엔진이 알아서 함.
                                                sheetStep = BottomSheetStep.HALF
                                                coroutineScope.launch { scaffoldState.bottomSheetState.partialExpand() }
                                            }
                                            BottomSheetStep.HALF -> {
                                                // 260dp -> 40dp로 축소
                                                sheetStep = BottomSheetStep.COLLAPSED
                                            }
                                            BottomSheetStep.COLLAPSED -> {}
                                        }
                                    }
                                },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    dragAccumulator += dragAmount
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.width(40.dp).height(4.dp).background(SubLightGray, RoundedCornerShape(2.dp)))
                }
            }
        },
        sheetContent = {
            HomeBottomSheetContent(
                storeList = storeList,
                isLoading = isLoading,
                activeFilter = activeFilter,
                sheetStep = sheetStep,
                onScrollDownAtTop = {
                    // ✅ 수정 핵심 2: FULL 상태일 때는 아무것도 하지 않음 (Compose 기본 동작에 위임)
                    if (sheetStep == BottomSheetStep.HALF) {
                        // HALF 상태에서 리스트를 당겨 내리면 COLLAPSED로 높이만 줄임
                        sheetStep = BottomSheetStep.COLLAPSED
                    }
                },
                onItemClick = { store ->
                    onNavigateToDetail(store.id)
                }
            )
        },
        containerColor = White
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            UserMapContent(
                cameraPositionState = cameraPositionState,
                locationSource = locationSource,
                storeList = storeList,
                trackingMode = trackingMode,
                isLoading = false,
                selectedStoreId = selectedStoreId,
                onStoreClick = { storeId ->
                    selectedStoreId = storeId
                    sheetStep = BottomSheetStep.COLLAPSED
                },
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
                    trackingMode = LocationTrackingMode.NoFollow
                    sheetStep = BottomSheetStep.COLLAPSED
                    if (selectedStoreId != null) selectedStoreId = null
                }
            )

            val targetAlpha = if (scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded && !isSearchActive && storeList.isNotEmpty()) 0.25f else 0f
            val animatedAlpha by animateFloatAsState(
                targetValue = targetAlpha,
                animationSpec = tween(durationMillis = 300),
                label = "BackgroundAlphaAnimation"
            )

            if (animatedAlpha > 0f) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = animatedAlpha)))
            }

            if (isSearchActive) {
                val center = cameraPositionState.position.target
                Box(modifier = Modifier.fillMaxSize().background(White)) {
                    UserSearchScreen(
                        onBackClick = {
                            isSearchActive = false
                            viewModel.clearSearch()
                        },
                        onStoreClick = { store ->
                            isSearchActive = false
                            viewModel.clearSearch()
                            trackingMode = LocationTrackingMode.None
                            selectedStoreId = store.id
                            sheetStep = BottomSheetStep.COLLAPSED
                            viewModel.fetchStoresInCurrentMap(
                                lat = store.latitude,
                                lng = store.longitude,
                                radius = 2.0,
                                userLat = currentUserLocation?.latitude,
                                userLng = currentUserLocation?.longitude
                            )
                            coroutineScope.launch {
                                cameraPositionState.stop()
                                cameraPositionState.animate(
                                    update = CameraUpdate.scrollTo(LatLng(store.latitude, store.longitude)),
                                    durationMs = 1000
                                )
                            }
                        },
                        onUniversityClick = { uniName ->
                            isSearchActive = false  // 1. 검색창 닫기
                            viewModel.clearSearch() // 2. 검색어 초기화 (선택사항)

                            // 3. 대학명으로 API 호출 (universityName 파라미터 사용)
                            viewModel.fetchStoresByUniversity(
                                uniName = uniName,
                                lat = center.latitude,
                                lng = center.longitude,
                                radius = 2.0, // 대학 주변 2km 탐색
                                userLat = currentUserLocation?.latitude,
                                userLng = currentUserLocation?.longitude
                            ) { firstStore ->
                                // 4. 결과가 있으면 해당 위치로 지도 이동
                                if (firstStore != null) {
                                    trackingMode = LocationTrackingMode.None
                                    coroutineScope.launch {
                                        cameraPositionState.stop()
                                        cameraPositionState.animate(
                                            update = CameraUpdate.scrollTo(LatLng(firstStore.latitude, firstStore.longitude)),
                                            durationMs = 1000
                                        )
                                    }
                                }
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
            else {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding() // 1. 시스템 상태바 높이만큼 자동 확보 (Notch 대응)
                        // ★ [수정] top = 0.dp -> 16.dp로 변경
                        // 상태바 바로 아래에서 16dp만큼 더 띄워서 '떠있는 느낌'을 줍니다.
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HomeSearchBar(
                        activeHeadCount = activeFilter,
                        onClearFilter = {
                            viewModel.clearHeadCountFilter()
                            onFilterCleared()
                            sheetStep = BottomSheetStep.HALF
                            val target = cameraPositionState.position.target
                            viewModel.fetchStoresInCurrentMap(
                                target.latitude,
                                target.longitude,
                                getCurrentRadius(),
                                currentUserLocation?.latitude,
                                currentUserLocation?.longitude
                            )
                        },
                        onSearchClick = {
                            isSearchActive = true

                            coroutineScope.launch {
                                scaffoldState.bottomSheetState.partialExpand()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isSheetExpanded && selectedStoreId == null) {
                        SearchHereButton(
                            isLoading = isLoading,
                            onClick = {
                                sheetStep = BottomSheetStep.HALF
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

                val isSheetPhysicallyExpanded = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded

                if (!isSearchActive && selectedStoreId == null && sheetStep != BottomSheetStep.FULL && !isSheetPhysicallyExpanded) {
                    CurrentLocationButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp)
                            // ★ 핵심: 바텀시트 높이(animatedPeekHeight) + 20dp 만큼 위로 띄움 -> 시트랑 같이 움직임
                            .padding(bottom = animatedPeekHeight + 20.dp),
                        isSelected = trackingMode == LocationTrackingMode.Follow,
                        onClick = { refreshCurrentLocation() }
                    )
                }

                if (selectedStoreId != null) {
                    val selectedIndex = storeList.indexOfFirst { it.id == selectedStoreId }
                    val selectedStore = storeList.getOrNull(selectedIndex)
                    if (selectedStore != null) {
                        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 50.dp)) {
                            StoreDetailCard(
                                index = selectedIndex + 1,
                                store = selectedStore,
                                onItemClick = { onNavigateToDetail(selectedStore.id) },
                                onCallClick = { IntentUtil.makePhoneCall(context, selectedStore.storePhone) }
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

// ★ [수정됨] 바텀시트 컨텐츠
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeBottomSheetContent(
    storeList: List<Store>,
    isLoading: Boolean,
    activeFilter: Int?,
    sheetStep: BottomSheetStep,
    onScrollDownAtTop: () -> Unit,
    onItemClick: (Store) -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val nestedScrollConnection = remember(listState, sheetStep) {
        object : NestedScrollConnection {
            // 1. 스크롤 되기 전 (헤더 처리 등)
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val isScrollingDown = delta > 0
                val isAtTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0

                // 상단에서 아래로 당길 때 바텀시트 단계 조절 (기존 로직)
                if (isScrollingDown && isAtTop) {
                    if (sheetStep == BottomSheetStep.HALF) {
                        onScrollDownAtTop()
                        return available
                    } else if (sheetStep == BottomSheetStep.COLLAPSED) {
                        return available
                    }
                }
                return Offset.Zero
            }

            // 2. ★ [핵심 해결] 스크롤 되고 난 후 (남은 스크롤 처리)
            // 리스트가 바닥에 닿아서 더 이상 스크롤할 수 없을 때 남은 available이 들어옵니다.
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                // 사용자가 위로 드래그 중(y < 0)인데 리스트가 다 내려가서 남은 스크롤이 있다면?
                if (available.y < 0) {
                    // 여기서 'available'을 전부 반환하면 "내가 다 썼어"라고 거짓말을 하게 됩니다.
                    // 결과적으로 부모(BottomSheetScaffold)에게는 스크롤이 전달되지 않아 시트가 들리지 않습니다.
                    return available
                }
                return Offset.Zero
            }
        }
    }

    // 오버스크롤(고무줄) 효과 제거
    CompositionLocalProvider(
        LocalOverscrollConfiguration provides null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // ★ 사용자님 의도대로 0.8f 높이 유지
                .then(
                    if (storeList.isNotEmpty()) Modifier.fillMaxHeight(0.8f)
                    else Modifier.height(260.dp)
                )
                .background(White)
        ) {
            if (storeList.isEmpty()) {
                // ... (빈 화면 로직 동일) ...
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        Text(text = "검색 중입니다...", style = MaterialTheme.typography.bodyMedium, color = SubGray)
                    } else {
                        val count = activeFilter
                        if (count != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(bottom = 20.dp)) {
                                Image(painter = painterResource(id = R.drawable.img_emptylist), contentDescription = null, modifier = Modifier.size(100.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = "지금은 '${count}명'이 앉을 수 있는 술집이 없어요", style = MaterialTheme.typography.bodyMedium, color = SubGray)
                            }
                        } else {
                            Text(text = "이 지역에 등록된 술집이 없습니다.", style = MaterialTheme.typography.bodyMedium, color = SubGray)
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection), // ★ 수정된 연결 객체 적용
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    itemsIndexed(storeList) { index, store ->
                        StoreListItem(
                            index = index + 1,
                            store = store,
                            onItemClick = { onItemClick(store) },
                            onCallClick = { IntentUtil.makePhoneCall(context, store.storePhone) }
                        )
                        if (index < storeList.lastIndex) {
                            HorizontalDivider(
                                color = SubPaleGray,
                                thickness = 1.dp,
                                modifier = Modifier
                                    .padding(horizontal = 24.dp)
                                    .padding(vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
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

// ★ [Preview]
@Preview(showBackground = true, name = "결과 없음 (4명 필터)")
@Composable
fun PreviewHomeBottomSheetEmptyFilter() {
    SeatNowTheme {
        HomeBottomSheetContent(
            storeList = emptyList(),
            isLoading = false,
            activeFilter = 4,
            sheetStep = BottomSheetStep.HALF, // ✅ [추가됨] Preview용 더미 데이터
            onScrollDownAtTop = {},
            onItemClick = {}
        )
    }
}

@Preview(showBackground = true, name = "결과 없음 (일반)")
@Composable
fun PreviewHomeBottomSheetEmptyNormal() {
    SeatNowTheme {
        HomeBottomSheetContent(
            storeList = emptyList(),
            isLoading = false,
            activeFilter = null,
            sheetStep = BottomSheetStep.HALF, // ✅ [추가됨] Preview용 더미 데이터
            onScrollDownAtTop = {},
            onItemClick = {}
        )
    }
}