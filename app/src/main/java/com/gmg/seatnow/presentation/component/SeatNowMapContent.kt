package com.gmg.seatnow.presentation.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.presentation.util.MapUtils
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationSource
import com.naver.maps.map.compose.CameraPositionState
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.MapEffect
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.overlay.OverlayImage

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun UserMapContent(
    cameraPositionState: CameraPositionState,
    locationSource: LocationSource,
    storeList: List<Store>,
    trackingMode: LocationTrackingMode,
    isLoading: Boolean = false,
    selectedStoreId: Long? = null,
    onStoreClick: (Long) -> Unit,
    onMapClick: () -> Unit,
    onSearchHereClick: () -> Unit,
    onCurrentLocationClick: () -> Unit,
    onMapGestured: () -> Unit
) {
    val context = LocalContext.current

    Box(modifier = Modifier.Companion.fillMaxSize()) {
        NaverMap(
            modifier = Modifier.Companion.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            locationSource = locationSource,
            onMapClick = { _, _ -> onMapClick() },
            uiSettings = MapUiSettings(
                isLocationButtonEnabled = false,
                isZoomControlEnabled = false
            ),
            properties = MapProperties(
                locationTrackingMode = trackingMode
            )
        ) {
            // ★ [원상복구] 겹침 계산 없이 리스트 전체를 순회하며 핀 생성
            storeList.forEachIndexed { index, store ->
                key(store.id) {
                    val isSelected = (store.id == selectedStoreId)

                    // 1. 비트맵 생성 (선택 여부에 따라)
                    val markerIcon = if (isSelected) {
                        MapUtils.createSelectedMarkerBitmap(context, index + 1, store.status)
                    } else {
                        MapUtils.createMarkerBitmap(context, index + 1, store.status)
                    }

                    // 2. 크기 애니메이션
                    val targetWidth = if (isSelected) 52.dp else 34.dp
                    val targetHeight = if (isSelected) 65.dp else 34.dp

                    val animatedWidth by animateDpAsState(
                        targetValue = targetWidth,
                        label = "Width",
                        animationSpec = tween(300)
                    )
                    val animatedHeight by animateDpAsState(
                        targetValue = targetHeight,
                        label = "Height",
                        animationSpec = tween(300)
                    )

                    Marker(
                        state = MarkerState(position = LatLng(store.latitude, store.longitude)),
                        captionText = if (isSelected) "" else store.name,
                        captionOffset = 5.dp,
                        icon = OverlayImage.fromBitmap(markerIcon),
                        width = animatedWidth,
                        height = animatedHeight,
                        zIndex = if (isSelected) 100 else 0,
                        onClick = {
                            onStoreClick(store.id)
                            true
                        }
                    )
                }
            }

            // ★ MapEffect는 반드시 NaverMap 블록 안에 위치해야 앱이 죽지 않습니다.
            MapEffect(Unit) { naverMap ->
                naverMap.addOnCameraChangeListener { reason, _ ->
                    if (reason == CameraUpdate.REASON_GESTURE) {
                        onMapGestured()
                    }
                }
            }
        }
    }
}