package com.gmg.seatnow.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gmg.seatnow.presentation.nav.SeatNowNavGraph
import com.gmg.seatnow.presentation.theme.SeatNowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // 👈 이거 필수입니다!
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SeatNowTheme {
                SeatNowNavGraph()
            }
        }
    }
}