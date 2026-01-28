package com.gmg.seatnow.presentation.owner.store.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.White

@Composable
fun EditAccountInfoScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            SeatNowTopAppBar(
                title = "계정 정보 수정",
                onBackClick = onBackClick
            )
        },
        containerColor = White,
        bottomBar = {
            // 하단 저장 버튼 (예시)
            Button(
                onClick = { /* TODO: 저장 로직 */ onBackClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PointRed)
            ) {
                Text(text = "저장하기", fontWeight = FontWeight.Bold, color = White)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            // [예시 UI] 입력 필드들
            Text("이메일", style = MaterialTheme.typography.labelMedium, color = SubGray)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            ) {
                // TextField 자리
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("비밀번호 변경", style = MaterialTheme.typography.labelMedium, color = SubGray)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            )
        }
    }
}