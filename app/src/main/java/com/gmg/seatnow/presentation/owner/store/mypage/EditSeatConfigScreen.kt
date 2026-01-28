package com.gmg.seatnow.presentation.owner.store.mypage

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.White

@Composable
fun EditSeatConfigScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            SeatNowTopAppBar(
                title = "좌석 정보 구성 수정",
                onBackClick = onBackClick
            )
        },
        containerColor = White
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "좌석 배치도 수정 UI가 들어갈 화면입니다.",
                color = SubGray
            )
        }
    }
}