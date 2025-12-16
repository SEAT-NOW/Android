package com.gmg.seatnow.presentation.splash

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.R
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SeatNowTheme

// 1. 실제 앱에서 쓰이는 화면 (로직 + UI 연결)
@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(), // Hilt 주입은 여기서만!
    onNavigateToLogin: () -> Unit,
    onNavigateToUserMain: () -> Unit
) {
    LaunchedEffect(true) {
        viewModel.event.collect { event ->
            when (event) {
                is SplashViewModel.SplashEvent.NavigateToLogin -> onNavigateToLogin()
                is SplashViewModel.SplashEvent.NavigateToUserMain -> onNavigateToUserMain()
            }
        }
    }

    // UI 그리는 부분은 아래 함수에게 위임
    SplashScreenContent()
}

// 2. 순수 UI 화면 (ViewModel 없음 -> 프리뷰 가능!)
@Composable
fun SplashScreenContent() {
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

// 3. 프리뷰 (UI 함수만 호출)
@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SeatNowTheme {
        // ViewModel이 필요한 SplashScreen() 대신
        // UI만 있는 SplashScreenContent()를 호출해야 합니다.
        SplashScreenContent()
    }
}