package com.gmg.seatnow.presentation.owner.signup.steps

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SeatNowTheme
import com.gmg.seatnow.presentation.theme.SubBlack
import com.gmg.seatnow.R

@Composable
fun Step6CompleteScreen() {
    // 화면 중앙 정렬을 위한 Column
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 100.dp, bottom = 50.dp), // 상단 여백을 주어 시각적 중앙 배치
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
//         1. 축하 아이콘 (이미지 리소스로 교체 권장)
        Image(
            painter = painterResource(R.drawable.ic_cong),
            contentDescription = "환영합니다",
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2. 환영 문구
        Text(
            text = "SEAT NOW에\n오신 것을",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            ),
            color = SubBlack,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "환영합니다!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = PointRed, // 빨간색 강조
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun PreviewStep6Complete() {
    SeatNowTheme {
        Step6CompleteScreen()
    }
}