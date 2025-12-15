package com.example.seatnow.presentation.splash // 👈 패키지명 확인

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seatnow.presentation.theme.PointRed
import com.example.seatnow.presentation.theme.SeatNowTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    LaunchedEffect(key1 = true) {
        delay(2000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PointRed),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "SEAT NOW" 로고 구성
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("SEAT", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("N", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
                Text("W", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true) // 배경색 흰색으로 보여줌
@Composable
fun SplashScreenPreview() {
    SeatNowTheme { // 테마를 감싸야 폰트/색상이 제대로 보입니다
        SplashScreen(onSplashFinished = {})
    }
}