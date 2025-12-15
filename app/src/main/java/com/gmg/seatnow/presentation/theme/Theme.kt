package com.gmg.seatnow.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 라이트 모드 컬러 팔레트 정의
private val LightColorScheme = lightColorScheme(
    // 1. 브랜드 메인 컬러
    primary = PointRed,             // 컴포넌트의 강조색 (버튼 배경, 활성 상태 등)
    onPrimary = White,              // Primary 위 글자색 (빨간 버튼 위 흰 글씨)
    primaryContainer = PointLightPink, // 옅은 강조 배경
    onPrimaryContainer = PointRed,     // 그 위의 글자

    // 2. 보조 컬러
    secondary = PointPink,
    onSecondary = White,
    secondaryContainer = PointLightPink,

    // 3. 배경 및 서피스 (카드, 시트 등)
    background = White,             // 앱 전체 배경
    onBackground = SubBlack,        // 배경 위 글자 (기본 텍스트 색상 #181717)

    surface = White,                // 카드나 다이얼로그 배경
    onSurface = SubBlack,           // 카드 위 글자

    surfaceVariant = SubPaleGray,   // 약간 구분되는 배경 (#F6F3F3 - 입력 필드 배경 등에 적합)
    onSurfaceVariant = SubDarkGray, // 그 위의 글자 (#504747)

    // 4. 테두리 및 기타
    outline = SubGray,              // 체크박스 테두리, 입력창 외곽선 (#A99E9E)
    outlineVariant = SubLightGray,  // 더 연한 구분선 (#DFD7D7)

    error = Color(0xFFBA1A1A)       // 에러 색상은 기본 빨강 유지 (필요시 변경 가능)
)

@Composable
fun SeatNowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color는 Android 12+에서 배경화면 색을 따라가는 기능인데,
    // 브랜드 컬러를 지키기 위해 false로 끄는 것을 추천합니다.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // 지금은 라이트 모드만 설정 (다크 모드 필요시 DarkColorScheme 별도 정의 필요)
    val colorScheme = LightColorScheme

    // 상태바(Status Bar) 색상 설정
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // 상태바 아이콘 색상: 배경이 밝으면 어둡게(true), 어두우면 밝게(false)
            // 여기서는 상태바를 흰색으로 할지, 포인트 컬러로 할지에 따라 다름
            // 보통 앱들은 흰색 배경에 검은 아이콘을 씁니다.
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // 이전에 만든 폰트 설정
        content = content
    )
}