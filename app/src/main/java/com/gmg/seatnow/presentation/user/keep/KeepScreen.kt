package com.gmg.seatnow.presentation.user.keep

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gmg.seatnow.presentation.theme.SubBlack
import com.gmg.seatnow.presentation.theme.White

@Composable
fun KeepScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "내가 킵한 가게들 (준비중)",
            color = SubBlack
        )
    }
}