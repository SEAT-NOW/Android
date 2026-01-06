package com.gmg.seatnow.presentation.user.home

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview // [추가] Preview Import
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.theme.*
import com.naver.maps.map.compose.*

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun UserHomeScreen() {
    // 네이버 지도 카메라 상태 관리 (초기 위치: 서울 홍대 입구 부근 예시)
    val cameraPositionState = rememberCameraPositionState {
        // 필요 시 초기 위치 지정 가능
    }

    Scaffold(
        bottomBar = { UserBottomNavigation() }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // 하단 바 높이만큼 패딩 적용
        ) {
            // 1. 네이버 지도 (가장 뒤에 배치)
            NaverMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    isLocationButtonEnabled = false, // 기본 버튼 숨기고 커스텀 버튼 사용
                    isZoomControlEnabled = false     // 줌 버튼 숨김 (필요시 true)
                ),
                locationSource = rememberFusedLocationSource(), // 현위치 추적용
                properties = MapProperties(
                    locationTrackingMode = LocationTrackingMode.NoFollow // 초기엔 추적 안함
                )
            ) {
                // 추후 여기에 마커(Marker) 추가 예정
            }

            // 2. 상단 검색창
            TopSearchBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            )

            // 3. "현 지도에서 검색" 버튼 (상단 검색창 아래)
            SearchHereButton(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp), // 검색창 높이 고려하여 아래로 배치
                onClick = { /* TODO: 지도 중심 좌표로 검색 API 호출 */ }
            )

            // 4. 현재 위치 버튼 (우측 하단)
            CurrentLocationButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 24.dp),
                onClick = {
                    // TODO: 현위치로 카메라 이동 로직
                }
            )
        }
    }
}

// --- 하위 컴포넌트 ---

@Composable
fun UserBottomNavigation() {
    var selectedItem by remember { mutableIntStateOf(0) }

    NavigationBar(
        containerColor = White,
        tonalElevation = 8.dp
    ) {
        // 1. 홈 탭
        NavigationBarItem(
            selected = selectedItem == 0,
            onClick = { selectedItem = 0 },
            icon = { Icon(Icons.Default.Home, contentDescription = "홈") },
            label = { Text("홈", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PointRed,
                selectedTextColor = PointRed,
                indicatorColor = Color.Transparent,
                unselectedIconColor = SubGray,
                unselectedTextColor = SubGray
            )
        )

        // 2. N명 자리찾기 탭
        NavigationBarItem(
            selected = selectedItem == 1,
            onClick = { selectedItem = 1 },
            icon = { Icon(Icons.Default.Place, contentDescription = "N명 자리찾기") },
            label = { Text("N명 자리찾기", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PointRed,
                selectedTextColor = PointRed,
                indicatorColor = Color.Transparent,
                unselectedIconColor = SubGray,
                unselectedTextColor = SubGray
            )
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
    onClick: () -> Unit
) {
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
            imageVector = Icons.Default.Person,
            contentDescription = "현재 위치",
            tint = SubBlack
        )
    }
}

// ★ [추가] Preview 구성
@Preview(showBackground = true)
@Composable
fun UserHomeScreenPreview() {
    SeatNowTheme {
        UserHomeScreen()
    }
}