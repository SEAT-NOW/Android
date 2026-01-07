package com.gmg.seatnow.presentation.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.CameraPositionState
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object MapLogicHandler {

    // 현재 위치를 찾고, 카메라를 이동시킨 뒤, 필요하면 API까지 호출하는 공통 함수
    @OptIn(ExperimentalNaverMapApi::class)
    fun moveCameraToCurrentLocation(
        context: Context,
        cameraPositionState: CameraPositionState,
        coroutineScope: CoroutineScope,
        onLocationFound: (Double, Double) -> Unit = { _, _ -> } // API 호출 등 후속 작업
    ) {
        // 권한 체크
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    // ★ 핵심 수정: move는 suspend 함수이므로 코루틴 스코프 필수
                    coroutineScope.launch {
                        cameraPositionState.move(CameraUpdate.scrollTo(LatLng(it.latitude, it.longitude)))
                    }
                    // 위치 찾은 후 콜백 실행 (API 호출 등)
                    onLocationFound(it.latitude, it.longitude)
                }
            }
        }
    }
}