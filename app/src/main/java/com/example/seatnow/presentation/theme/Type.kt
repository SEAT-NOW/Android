package com.example.seatnow.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.seatnow.R

// 1. 폰트 패밀리 (Pretendard)
val Pretendard = FontFamily(
    Font(R.font.pretendard_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.pretendard_regular, FontWeight.Normal, FontStyle.Normal)
)

// =================================================================
// 2. 피그마 스타일 전체 정의 (변수로 다 만들어둡니다)
// =================================================================

// --- Headline (Bold) ---
val Headline_Bold_32 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp)
val Headline_Bold_24 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp)
val Headline_Bold_20 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 28.sp)

// --- Title1 (Bold) ---
val Title1_Bold_16 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 24.sp)
val Title1_Bold_14 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp)
val Title1_Bold_12 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp)

// --- Subtitle1 (SemiBold) ---
val Subtitle1_SemiBold_16 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp)
val Subtitle1_SemiBold_14 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)
val Subtitle1_SemiBold_12 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp)

// --- Body1 (Medium) ---
val Body1_Medium_16 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp)
val Body1_Medium_14 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp)
val Body1_Medium_12 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)
val Body1_Medium_8 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 8.sp, lineHeight = 12.sp) // 👈 Material에 없는 사이즈

// --- Body2 (Regular) ---
// ⚠️ Material은 Body 스타일이 1개 세트(Large/Medium/Small) 뿐이라 얘는 매핑할 자리가 없습니다.
// 그냥 변수로 쓰면 됩니다.
val Body2_Regular_16 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp)
val Body2_Regular_14 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp)
val Body2_Regular_12 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp)

// --- Button (Custom) ---
val Button_Bold_16 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 24.sp)
val Button_SemiBold_16 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp)
val Button_Regular_14 = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp)


// =================================================================
// 3. Material Typography 매핑 (억지로 다 넣지 말고, 맞는 것만!)
// =================================================================
val Typography = Typography(
    // [Headline] -> Headline 계열 매핑
    headlineLarge = Headline_Bold_32,
    headlineMedium = Headline_Bold_24,
    headlineSmall = Headline_Bold_20,

    // [Title1] -> Title 계열 매핑
    titleLarge = Title1_Bold_16,
    titleMedium = Title1_Bold_14,
    titleSmall = Title1_Bold_12,

    // [Subtitle1] -> Label 계열 매핑 (보통 Label이 작은 제목이나 버튼 텍스트용)
    labelLarge = Subtitle1_SemiBold_16,
    labelMedium = Subtitle1_SemiBold_14,
    labelSmall = Subtitle1_SemiBold_12,

    // [Body1] -> Body 계열 매핑
    bodyLarge = Body1_Medium_16,
    bodyMedium = Body1_Medium_14,
    bodySmall = Body1_Medium_12

    // ⚠️ 남은 것들 (Body2, Body1_8px, Button 스타일 등)은 연결 안 함!
    // 억지로 Display 같은 곳에 넣으면 나중에 더 헷갈립니다.
)