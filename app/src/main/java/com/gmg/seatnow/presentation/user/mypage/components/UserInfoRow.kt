package com.gmg.seatnow.presentation.user.mypage.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.theme.SubBlack
import com.gmg.seatnow.presentation.theme.SubDarkGray
import com.gmg.seatnow.presentation.theme.SubGray

// 정보 한 줄을 표시하는 컴포넌트 (타이틀 --- 여백 --- 데이터)
@Composable
fun UserInfoRow(title: String, value: String, showArrow: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = SubBlack
        )
        // [핵심] 1f 만큼의 가중치를 주어 텍스트 사이를 최대한 벌림
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = SubDarkGray
        )

        if (showArrow) {
            Spacer(modifier = Modifier.width(4.dp)) // 텍스트와 화살표 사이 간격
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = SubGray, // 화살표 색상은 연하게
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
