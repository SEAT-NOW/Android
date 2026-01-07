package com.gmg.seatnow.presentation.user.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.theme.*
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.*
import com.gmg.seatnow.R
import com.gmg.seatnow.presentation.util.MapUtils.createMarkerBitmap
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.OverlayImage
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun UserHomeScreen(
    viewModel: UserHomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    // 카메라 상태
    val cameraPositionState = rememberCameraPositionState {}

    // 위치 추적 모드 (초기엔 None, 위치 잡으면 Follow)
    var trackingMode by remember { mutableStateOf(LocationTrackingMode.None) }

    // FusedLocationClient (위치 가져오기용)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val storeList by viewModel.storeList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 초기 데이터 로딩 여부 플래그
    var isInitialLoaded by remember { mutableStateOf(false) }

    // ★ [핵심 함수] 현재 위치를 1회 가져와서 -> 지도 이동 및 API 호출
    fun fetchCurrentLocationAndLoadData() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    // 1. 카메라를 즉시 사용자 위치로 이동 (애니메이션 없이 즉시 이동하여 갭 제거)
                    cameraPositionState.move(CameraUpdate.scrollTo(LatLng(it.latitude, it.longitude)))

                    // 2. 추적 모드 켜기 (파란 점 따라다니기)
                    trackingMode = LocationTrackingMode.Follow

                    // 3. API 호출 (이 시점은 무조건 내 위치임이 보장됨)
                    if (!isInitialLoaded) {
                        viewModel.fetchStoresInCurrentMap(it.latitude, it.longitude)
                        isInitialLoaded = true
                    }
                }
            }
        }
    }

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            // 권한 허용 시 즉시 로직 실행
            fetchCurrentLocationAndLoadData()
        }
    }

    // 1. 화면 진입 시 권한 체크 및 실행
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // 이미 권한이 있으면 바로 실행
            fetchCurrentLocationAndLoadData()
        } else {
            // 권한 없으면 요청
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isPreview) {
            Box(Modifier.fillMaxSize().background(Color.LightGray))
        } else {
            NaverMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    isLocationButtonEnabled = false, // 커스텀 버튼 사용하므로 false
                    isZoomControlEnabled = false
                ),
                locationSource = rememberFusedLocationSource(),
                properties = MapProperties(
                    locationTrackingMode = trackingMode
                )
            ) {
                storeList.forEachIndexed { index, store ->
                    if (index < 10) {
                        val markerIcon = createMarkerBitmap(index + 1, store.status)
                        Marker(
                            state = MarkerState(position = LatLng(store.latitude, store.longitude)),
                            captionText = store.name,
                            icon = OverlayImage.fromBitmap(markerIcon),
                            onClick = { true }
                        )
                    }
                }

                // 제스처 감지 시 추적 모드 해제 (자유 이동)
                MapEffect(Unit) { naverMap ->
                    naverMap.addOnCameraChangeListener { reason, _ ->
                        if (reason == CameraUpdate.REASON_GESTURE) {
                            trackingMode = LocationTrackingMode.NoFollow
                        }
                    }
                }
            }
        }

        // 현 지도에서 검색 버튼
        SearchHereButton(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp),
            isLoading = isLoading,
            onClick = {
                val center = cameraPositionState.position.target
                viewModel.fetchStoresInCurrentMap(center.latitude, center.longitude)
            }
        )

        // 현재 위치 버튼
        CurrentLocationButton(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 24.dp),
            isSelected = trackingMode == LocationTrackingMode.Follow,
            onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                    // 버튼 클릭 시: 내 위치로 이동 + 데이터 갱신까지 원하시면 아래 함수 호출
                    fetchCurrentLocationAndLoadData()

                    // 만약 버튼 클릭 시 '이동'만 하고 싶다면:
                    // trackingMode = LocationTrackingMode.Follow
                } else {
                    permissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                }
            }
        )
    }
}

@Composable
fun TopSearchBar(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = White
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "검색",
                tint = PointRed
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "장소, 지역, 대학명 검색",
                style = MaterialTheme.typography.bodyMedium,
                color = SubGray
            )
        }
    }
}

@Composable
fun SearchHereButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(50),
        color = White,
        shadowElevation = 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = PointRed,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "검색 중...",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = PointRed
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = PointRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "현 지도에서 검색",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = PointRed
                )
            }
        }
    }
}

@Composable
fun CurrentLocationButton(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconColor = if (isSelected) PointRed else SubDarkGray

    Box(
        modifier = modifier
            .size(48.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_current),
            contentDescription = "현재 위치",
            tint = iconColor // SubBlack 대신 기본 Black 사용
        )
    }
}

// ★ Preview (이제 정상적으로 렌더링 됩니다)
@Preview(showBackground = true)
@Composable
fun UserMainScreenPreview() {
    SeatNowTheme {
        UserMainScreen()
    }
}