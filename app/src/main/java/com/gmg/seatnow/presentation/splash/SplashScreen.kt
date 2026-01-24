package com.gmg.seatnow.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.R
import com.gmg.seatnow.presentation.theme.PointRed

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToUserMain: () -> Unit,
    onNavigateToOwnerMain: () -> Unit, // [추가] 사장님 메인 콜백
    onNavigateToTerms: (Boolean) -> Unit
) {
    LaunchedEffect(true) {
        viewModel.event.collect { event ->
            when (event) {
                is SplashViewModel.SplashEvent.NavigateToLogin -> onNavigateToLogin()
                is SplashViewModel.SplashEvent.NavigateToUserMain -> onNavigateToUserMain()
                is SplashViewModel.SplashEvent.NavigateToOwnerMain -> onNavigateToOwnerMain() // [추가]
                is SplashViewModel.SplashEvent.NavigateToTerms -> onNavigateToTerms(event.isGuest)
            }
        }
    }

    // UI 렌더링 (기존과 동일)
    Box(
        modifier = Modifier.fillMaxSize().background(PointRed),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_splash_logo),
            contentDescription = "SeatNow Splash Logo",
            modifier = Modifier.width(180.dp)
        )
    }
}