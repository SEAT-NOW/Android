package com.gmg.seatnow.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gmg.seatnow.R

// 1. 폰트 패밀리
val Pretendard = FontFamily(
    Font(R.font.pretendard_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.pretendard_regular, FontWeight.Normal, FontStyle.Normal)
)

// 자간 설정 (이 값을 조절하면 앱 전체 자간이 바뀝니다)
private val WideSpacing = 0.5.sp

// =================================================================
// 2. 피그마 스타일 정의 (letterSpacing 추가됨)
// =================================================================

// --- Headline (Bold) ---
val Headline_Bold_32 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = WideSpacing)
val Headline_Bold_24 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = WideSpacing)
val Headline_Bold_20 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 28.sp, letterSpacing = WideSpacing)

// --- Title1 (Bold) ---
val Title1_Bold_16 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = WideSpacing)
val Title1_Bold_14 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = WideSpacing)
val Title1_Bold_12 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = WideSpacing)

// --- Subtitle1 (SemiBold) ---
val Subtitle1_SemiBold_16 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = WideSpacing)
val Subtitle1_SemiBold_14 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = WideSpacing)
val Subtitle1_SemiBold_12 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = WideSpacing)

// --- Body1 (Medium) ---
val Body1_Medium_16 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = WideSpacing)
val Body1_Medium_14 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = WideSpacing)
val Body1_Medium_12 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = WideSpacing)
val Body1_Medium_8 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 8.sp, lineHeight = 12.sp, letterSpacing = WideSpacing)

// --- Body2 (Regular) ---
val Body2_Regular_16 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = WideSpacing)
val Body2_Regular_14 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = WideSpacing)
val Body2_Regular_12 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = WideSpacing)


// =================================================================
// 3. Material Typography 매핑
// =================================================================
val Typography = Typography(
    headlineLarge = Headline_Bold_32,
    headlineMedium = Headline_Bold_24,
    headlineSmall = Headline_Bold_20,

    titleLarge = Title1_Bold_16,
    titleMedium = Title1_Bold_14,
    titleSmall = Title1_Bold_12,

    // 버튼 등에 쓰일 Label
    labelLarge = Subtitle1_SemiBold_16,
    labelMedium = Subtitle1_SemiBold_14,
    labelSmall = Subtitle1_SemiBold_12,

    bodyLarge = Body1_Medium_16,
    bodyMedium = Body1_Medium_14,
    bodySmall = Body1_Medium_12
)