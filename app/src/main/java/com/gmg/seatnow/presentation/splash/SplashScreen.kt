package com.gmg.seatnow.presentation.splash // 👈 패키지명 확인

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.R
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SeatNowTheme
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
        Image(
            painter = painterResource(id = R.drawable.ic_splash_logo),
            contentDescription = "SeatNow Logo",
            modifier = Modifier.width(200.dp)
        )
    }
}

@Preview(showBackground = true) // 배경색 흰색으로 보여줌
@Composable
fun SplashScreenPreview() {
    SeatNowTheme { // 테마를 감싸야 폰트/색상이 제대로 보입니다
        SplashScreen(onSplashFinished = {})
    }
}