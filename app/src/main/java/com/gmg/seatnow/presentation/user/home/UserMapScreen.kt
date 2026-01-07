package com.gmg.seatnow.presentation.user.home

import android.Manifest
import android.content.pm.PackageManager
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
import com.gmg.seatnow.presentation.theme.*
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.*
import com.gmg.seatnow.R

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun UserMapScreen() {
    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState {}

    var trackingMode by remember { mutableStateOf(LocationTrackingMode.Follow) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // 권한 허용 시 -> 즉시 Follow 모드 발동 (지도가 움직임)
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            trackingMode = LocationTrackingMode.Follow
        }
    }

    // 4. 화면 진입 시 권한 체크 & 요청
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한 없으면 물어보기
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // ★ Preview 렌더링 문제 해결: 미리보기 모드인지 확인
    val isPreview = LocalInspectionMode.current


    Box(modifier = Modifier.fillMaxSize()) {

        // (1) 지도 영역
        if (isPreview) {
            // 미리보기에서는 지도를 그리지 않고 회색 박스로 대체
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Naver Map (Preview Mode)", color = Color.DarkGray)
            }
        } else {
            // 실제 기기에서만 지도 렌더링
            NaverMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    isLocationButtonEnabled = false,
                    isZoomControlEnabled = false
                ),
                locationSource = rememberFusedLocationSource(),
                properties = MapProperties(
                    locationTrackingMode = trackingMode
                )
            ) {
                MapEffect(Unit) { naverMap ->
                    naverMap.addOnCameraChangeListener { reason, _ ->
                        // 사용자의 제스처(드래그, 핀치 등)로 카메라가 움직인 경우
                        if (reason == CameraUpdate.REASON_GESTURE) {
                            trackingMode = LocationTrackingMode.NoFollow
                        }
                    }
                }
            }
        }

        // (2) 상단 검색창 (MVP 제외 요청으로 주석 처리)
        /*
        TopSearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        )
        */

        // (3) "현 지도에서 검색" 버튼 (MVP 제외 요청으로 주석 처리)
        SearchHereButton(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp),
            onClick = { /* TODO : 검색로직 */ }
        )


        // (4) 현재 위치 버튼
        CurrentLocationButton(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 24.dp),
            isSelected = trackingMode == LocationTrackingMode.Follow,
            onClick = {
                // 버튼 누르면 권한 다시 체크하고 이동
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                    trackingMode = LocationTrackingMode.Follow
                } else {
                    // 권한 없으면 다시 요청
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