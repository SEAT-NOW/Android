package com.gmg.seatnow.presentation.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.presentation.theme.ColorFull
import com.gmg.seatnow.presentation.theme.ColorHard
import com.gmg.seatnow.presentation.theme.ColorNormal
import com.gmg.seatnow.presentation.theme.ColorSpare

object MapUtils {

    /**
     * [Default 핀]
     * 방식: 하얀색 핀을 바닥에 깔고, 그 위에 85% 크기의 색깔 핀을 얹음
     * 결과: 자연스럽고 선명한 하얀 테두리 생성
     */
    fun createMarkerBitmap(context: Context, number: Int, status: StoreStatus): Bitmap {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_pin_default)
            ?: return createFallbackBitmap(number)

        val size = 100 // 전체 크기 (px)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. 하얀색 배경 그리기 (테두리 역할)
        drawable.setTint(android.graphics.Color.WHITE)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)

        // 2. 상태 색상 내부 그리기 (약간 작게)
        val borderSize = 10 // 테두리 두께 조절 (숫자가 클수록 테두리가 두꺼워짐)
        val innerSize = size - borderSize

        val statusColor = getStatusColor(status)
        drawable.setTint(statusColor)

        // 중앙에 오도록 좌표 계산
        val offset = borderSize / 2
        drawable.setBounds(offset, offset, size - offset, size - offset)
        drawable.draw(canvas)

        // 3. 숫자 그리기
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

        // 비율 유지 (45:56 -> 135:168)
        val width = 135
        val height = 168

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. 하얀색 배경 그리기 (테두리 역할)
        drawable.setTint(android.graphics.Color.WHITE)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        // 2. 상태 색상 내부 그리기 (축소해서 얹기)
        val scale = 0.88f // 내부 핀 크기 비율 (0.88 = 88%)

        val innerWidth = (width * scale).toInt()
        val innerHeight = (height * scale).toInt()
        val dx = (width - innerWidth) / 2
        val dy = (height - innerHeight) / 2

        val statusColor = getStatusColor(status)
        drawable.setTint(statusColor)
        drawable.setBounds(dx, dy, dx + innerWidth, dy + innerHeight)
        drawable.draw(canvas)

        // 3. 숫자 그리기
        // 물방울 모양은 무게중심이 약간 위쪽에 있으므로 높이 보정 (height / 2.4f)
        drawCenteredText(canvas, number.toString(), width / 2f, height / 2.4f, 50f)

        return bitmap
    }

    // 상태별 컬러 반환 헬퍼
    private fun getStatusColor(status: StoreStatus): Int {
        return when (status) {
            StoreStatus.SPARE -> ColorSpare.toArgb()
            StoreStatus.NORMAL -> ColorNormal.toArgb()
            StoreStatus.HARD -> ColorHard.toArgb()
            StoreStatus.FULL -> ColorFull.toArgb()
        }
    }

    // 텍스트 중앙 정렬 헬퍼
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
        canvas.drawCircle(size/2f, size/2f, size/2f, paint)
        return bitmap
    }
}