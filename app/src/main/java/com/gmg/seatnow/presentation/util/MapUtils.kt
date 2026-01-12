package com.gmg.seatnow.presentation.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.presentation.theme.ColorFull
import com.gmg.seatnow.presentation.theme.ColorHard
import com.gmg.seatnow.presentation.theme.ColorNormal
import com.gmg.seatnow.presentation.theme.ColorSpare

object MapUtils {

    // 숫자를 넣으면 마커 비트맵을 뱉어주는 함수
    fun createMarkerBitmap(number: Int, status: StoreStatus): Bitmap {
        val size = 90 // 마커 크기 (px)
        val borderSize = 4f

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val center = size / 2f
        val radius = size / 2f

        // 1. 흰색 테두리 (가장 큰 원) 그리기
        paint.color = android.graphics.Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawCircle(center, center, radius, paint)

        // 2. 상태별 색상 원 (흰색 테두리 안쪽에 조금 작게 그리기)
        val innerRadius = radius - borderSize
        paint.color = when (status) {
            StoreStatus.SPARE -> ColorSpare.toArgb()
            StoreStatus.NORMAL -> ColorNormal.toArgb()
            StoreStatus.HARD -> ColorHard.toArgb()
            StoreStatus.FULL -> ColorFull.toArgb()
        }
        canvas.drawCircle(center, center, innerRadius, paint)

        // 3. 숫자 그리기
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 40f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER

        val text = number.toString()
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        // 텍스트 정중앙 배치 (Baseline 보정)
        val y = center - bounds.exactCenterY()
        canvas.drawText(text, center, y, paint)

        return bitmap
    }
}
