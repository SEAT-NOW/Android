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
import androidx.compose.foundation.layout.fillMaxSize
import com.gmg.seatnow.presentation.component.UserMapContent
import com.gmg.seatnow.presentation.util.MapLogicHandler

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun UserHomeScreen(
    viewModel: UserHomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState()

    // 위치 추적 상태 관리
    var trackingMode by remember { mutableStateOf(LocationTrackingMode.None) }

    val storeList by viewModel.storeList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 초기 로딩 여부 체크
    var isInitialLoaded by remember { mutableStateOf(false) }

    // [공통 로직 사용] 내 위치로 이동 후 -> 데이터 로드
    fun moveToLocationAndLoad() {
        MapLogicHandler.moveCameraToCurrentLocation(
            context = context,
            cameraPositionState = cameraPositionState,
            coroutineScope = coroutineScope,
            onLocationFound = { lat, lng ->
                trackingMode = LocationTrackingMode.Follow

                // 처음 진입 시에만 API 호출 (또는 필요에 따라 항상 호출)
                if (!isInitialLoaded) {
                    viewModel.fetchStoresInCurrentMap(lat, lng)
                    isInitialLoaded = true
                }
            }
        )
    }

    // 권한 요청 결과 처리
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            moveToLocationAndLoad()
        }
    }

    // 화면 진입 시 권한 체크 및 초기화
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            moveToLocationAndLoad()
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    // UI 렌더링 (UserMapContent 하나로 끝)
    Box(modifier = Modifier.fillMaxSize()) {
        UserMapContent(
            cameraPositionState = cameraPositionState,
            storeList = storeList,
            isLocationTracking = (trackingMode == LocationTrackingMode.Follow),
            isLoading = isLoading,
            onSearchHereClick = {
                // 재검색: 현재 지도 중심 좌표로 API 호출
                val center = cameraPositionState.position.target
                viewModel.fetchStoresInCurrentMap(center.latitude, center.longitude)
                trackingMode = LocationTrackingMode.None // 지도 움직였으니 트래킹 해제
            },
            onCurrentLocationClick = {
                // 현재 위치 버튼: 위치 찾고 카메라 이동
                moveToLocationAndLoad()
            }
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