package com.gmg.seatnow.presentation.user.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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

    val cameraPositionState = rememberCameraPositionState()
    val locationSource = rememberFusedLocationSource()
    var trackingMode by remember { mutableStateOf(LocationTrackingMode.None) }

    val storeList by viewModel.storeList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val activeFilter by viewModel.activeHeadCount.collectAsState()

    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        viewModel.clearSearch()
    }

    // ★ [수정됨] 기본 높이 45dp로 변경
    var targetPeekHeight by remember { mutableStateOf(45.dp) }

    LaunchedEffect(isLoading, isSearchActive, selectedStoreId, storeList.size) {
        targetPeekHeight = when {
            isSearchActive -> 0.dp
            selectedStoreId != null -> 45.dp // ★ [수정됨] 핀 선택 시에도 45dp
            isLoading -> 260.dp
            else -> 260.dp
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

    LaunchedEffect(selectedStoreId) {
        if (selectedStoreId != null) {
            if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                scaffoldState.bottomSheetState.partialExpand()
            }
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
        sheetSwipeEnabled = !isSearchActive && storeList.isNotEmpty(),
        sheetDragHandle = {
            if (!isSearchActive) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(48.dp).background(Color.Transparent)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { change, dragAmount ->
                                change.consume()
                                val threshold = 5f
                                if (dragAmount < -threshold) {
                                    if (storeList.isNotEmpty()) {
                                        if (scaffoldState.bottomSheetState.currentValue != SheetValue.Expanded) {
                                            coroutineScope.launch { scaffoldState.bottomSheetState.expand() }
                                        }
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
            HomeBottomSheetContent(
                storeList = storeList,
                isLoading = isLoading,
                activeFilter = activeFilter,
                onItemClick = { store ->
                    selectedStoreId = store.id
                    trackingMode = LocationTrackingMode.None
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdate.scrollTo(LatLng(store.latitude, store.longitude)),
                            durationMs = 800
                        )
                    }
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
                    // ★ [수정됨] 45dp로 변경 (제스처 시 내려가는 높이)
                    if (targetPeekHeight > 45.dp && !isSearchActive) {
                        trackingMode = LocationTrackingMode.NoFollow
                        if (storeList.isNotEmpty()) {
                            targetPeekHeight = 45.dp
                            if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                coroutineScope.launch { scaffoldState.bottomSheetState.partialExpand() }
                            }
                        } else {
                            targetPeekHeight = 45.dp
                        }
                    } else {
                        trackingMode = LocationTrackingMode.NoFollow
                    }
                    if (selectedStoreId != null) selectedStoreId = null
                }
            )

            if (isSheetExpanded && !isSearchActive && storeList.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)))
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
                    modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 0.dp, start = 16.dp, end = 16.dp),
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

                if (!isSheetExpanded && selectedStoreId == null) {
                    CurrentLocationButton(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 80.dp),
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

// ★ [수정됨] 바텀시트 컨텐츠
@Composable
fun HomeBottomSheetContent(
    storeList: List<Store>,
    isLoading: Boolean,
    activeFilter: Int?,
    onItemClick: (Store) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (storeList.isNotEmpty()) Modifier.fillMaxHeight(0.8f)
                else Modifier.height(260.dp)
            )
            .background(White)
    ) {
        if (storeList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    Text(
                        text = "검색 중입니다...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubGray
                    )
                } else {
                    val count = activeFilter
                    if (count != null) {
                        // [Case 1] N명 자리찾기 결과 없음 (이미지 포함)
                        // ★ [수정됨] 여기에만 bottom padding 20dp 추가
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_emptylist),
                                contentDescription = null,
                                modifier = Modifier.size(100.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "지금은 '${count}명'이 앉을 수 있는 술집이 없어요",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SubGray
                            )
                        }
                    } else {
                        // [Case 2] 일반 지도 검색 결과 없음
                        Text(
                            text = "이 지역에 등록된 술집이 없습니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SubGray
                        )
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp)) {
                itemsIndexed(storeList) { index, store ->
                    StoreListItem(
                        index = index + 1,
                        store = store,
                        onItemClick = { onItemClick(store) }
                    )
                    HorizontalDivider(color = SubPaleGray, thickness = 1.dp)
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
            onItemClick = {}
        )
    }
}