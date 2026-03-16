package com.gmg.seatnow.presentation.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.presentation.theme.ColorFull
import com.gmg.seatnow.presentation.theme.ColorHard
import com.gmg.seatnow.presentation.theme.ColorNormal
import com.gmg.seatnow.presentation.theme.ColorSpare
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.CameraPositionState
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object MapUtils {

    // ─────────────────────────────────────────────
    // 마커 비트맵 생성
    // ─────────────────────────────────────────────

    /**
     * [Default 핀]
     * 방식: 하얀색 핀을 바닥에 깔고, 그 위에 85% 크기의 색깔 핀을 얹음
     * 결과: 자연스럽고 선명한 하얀 테두리 생성
     */
    fun createMarkerBitmap(context: Context, number: Int, status: StoreStatus): Bitmap {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_pin_default)
            ?: return createFallbackBitmap(number)

        val size = 100
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawable.setTint(android.graphics.Color.WHITE)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)

        val borderSize = 10
        val statusColor = getStatusColor(status)
        drawable.setTint(statusColor)

        val offset = borderSize / 2
        drawable.setBounds(offset, offset, size - offset, size - offset)
        drawable.draw(canvas)

        drawCenteredText(canvas, number.toString(), size / 2f, size / 2f, 40f)

        return bitmap
    }

    /**
     * [Selected 핀]
     * 방식: 하얀색 물방울을 바닥에 깔고, 그 위에 88% 크기의 색깔 물방울을 얹음
     */
    fun createSelectedMarkerBitmap(context: Context, number: Int, status: StoreStatus): Bitmap {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_pin_selected)
            ?: return createFallbackBitmap(number)

        val width = 135
        val height = 168

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawable.setTint(android.graphics.Color.WHITE)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        val scale = 0.88f
        val innerWidth = (width * scale).toInt()
        val innerHeight = (height * scale).toInt()
        val dx = (width - innerWidth) / 2
        val dy = (height - innerHeight) / 2

        val statusColor = getStatusColor(status)
        drawable.setTint(statusColor)
        drawable.setBounds(dx, dy, dx + innerWidth, dy + innerHeight)
        drawable.draw(canvas)

        drawCenteredText(canvas, number.toString(), width / 2f, height / 2.4f, 50f)

        return bitmap
    }

    // ─────────────────────────────────────────────
    // 카메라 / 위치 제어 (구 MapLogicHandler 병합)
    // ─────────────────────────────────────────────

    /**
     * 현재 위치를 찾아 카메라를 이동시키고, 찾은 좌표로 콜백을 실행하는 공통 함수.
     * 기존 MapLogicHandler.moveCameraToCurrentLocation 병합.
     */
    @OptIn(ExperimentalNaverMapApi::class)
    fun moveCameraToCurrentLocation(
        context: Context,
        cameraPositionState: CameraPositionState,
        coroutineScope: CoroutineScope,
        onLocationFound: (Double, Double) -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    coroutineScope.launch {
                        cameraPositionState.move(CameraUpdate.scrollTo(LatLng(it.latitude, it.longitude)))
                    }
                    onLocationFound(it.latitude, it.longitude)
                }
            }
        }
    }

    // ─────────────────────────────────────────────
    // 내부 헬퍼
    // ─────────────────────────────────────────────

    private fun getStatusColor(status: StoreStatus): Int {
        return when (status) {
            StoreStatus.SPARE  -> ColorSpare.toArgb()
            StoreStatus.NORMAL -> ColorNormal.toArgb()
            StoreStatus.HARD   -> ColorHard.toArgb()
            StoreStatus.FULL   -> ColorFull.toArgb()
        }
    }

    private fun drawCenteredText(canvas: Canvas, text: String, x: Float, y: Float, textSize: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            this.textSize = textSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val textY = y - ((paint.descent() + paint.ascent()) / 2)
        canvas.drawText(text, x, textY, paint)
    }

    private fun createFallbackBitmap(number: Int): Bitmap {
        val size = 90
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply { color = android.graphics.Color.GRAY }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        return bitmap
    }
}