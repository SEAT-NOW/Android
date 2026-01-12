package com.gmg.seatnow.presentation.user.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.theme.*
import com.naver.maps.map.compose.*
import com.gmg.seatnow.presentation.user.UserMainScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.component.HomeSearchBar
import com.gmg.seatnow.presentation.component.SearchHereButton
import com.gmg.seatnow.presentation.component.UserMapContent
import com.gmg.seatnow.presentation.util.MapLogicHandler

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun UserHomeScreen(
    initialHeadCount: Int? = null, // Main에서 전달받은 필터 값
    onFilterCleared: () -> Unit,   // 필터 해제 콜백
    viewModel: UserHomeViewModel = hiltViewModel()
) {
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

    fun refreshCurrentLocation() {
        MapLogicHandler.moveCameraToCurrentLocation(
            context = context,
            cameraPositionState = cameraPositionState,
            coroutineScope = coroutineScope,
            onLocationFound = { lat, lng ->
                // 위치 찾으면 API 호출 및 트래킹 모드 변경
                viewModel.fetchStoresInCurrentMap(lat, lng)
                trackingMode = LocationTrackingMode.Follow
            }
        )
    }

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            refreshCurrentLocation() // 권한 허용 시 즉시 이동
        }
    }

    // [1. 초기 진입 로직] 권한 체크 후 바로 내 위치로 이동 (Focus)
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            refreshCurrentLocation()
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    // [2. 필터(N명) 적용 로직] 탭 이동으로 필터가 전달되면 적용 후 재검색
    LaunchedEffect(initialHeadCount) {
        if (initialHeadCount != null) {
            viewModel.setHeadCountFilter(initialHeadCount)
            // 권한이 있다면 내 위치 기준으로 다시 검색 (필터 적용된 상태로)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                refreshCurrentLocation()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. 지도 컨텐츠
        UserMapContent(
            cameraPositionState = cameraPositionState,
            locationSource = locationSource,
            storeList = storeList,
            trackingMode = trackingMode,
            isLoading = false, // 로딩바는 버튼 자체에서 처리하거나 별도 처리
            onSearchHereClick = { /* UserMapContent 내부 버튼 사용 안함, 오버레이로 구현 */ },
            onCurrentLocationClick = {
                refreshCurrentLocation()
            },
            onMapGestured = { trackingMode = LocationTrackingMode.NoFollow }
        )

        // 2. 상단 UI 오버레이 (검색바 + 현 지도에서 검색 버튼)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // [검색바]
            HomeSearchBar(
                activeHeadCount = activeFilter,
                onClearFilter = {
                    viewModel.clearHeadCountFilter() // ViewModel 상태 초기화
                    onFilterCleared() // 상위(Main) 상태 초기화 알림 (선택적)
                    // 필터 해제 후 현재 위치로 재검색
                    val target = cameraPositionState.position.target
                    viewModel.fetchStoresInCurrentMap(target.latitude, target.longitude)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // [현 지도에서 검색 버튼]
            SearchHereButton(
                isLoading = isLoading,
                onClick = {
                    val target = cameraPositionState.position.target
                    viewModel.fetchStoresInCurrentMap(target.latitude, target.longitude)
                }
            )
        }
    }
}