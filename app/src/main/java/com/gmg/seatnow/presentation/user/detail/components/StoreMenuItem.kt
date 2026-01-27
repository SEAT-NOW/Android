package com.gmg.seatnow.presentation.user.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Color.Unspecified 사용을 위한 임포트
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.MenuItemUiModel
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.presentation.util.IntentUtil

@Composable
fun StoreMenuItem(
    item: MenuItemUiModel,
    onLikeClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* 메뉴 상세 액션 */ }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (item.isRecommended) {
                Icon(
                    painter = painterResource(R.drawable.tag_recommend),
                    contentDescription = "추천",
                    tint = Color.Unspecified,
                    modifier = Modifier.height(24.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(text = item.name, style = Body1_Medium_14, fontWeight = FontWeight.Bold, color = SubBlack)
            Spacer(modifier = Modifier.height(12.dp))

            Text(text = IntentUtil.formatPrice(item.price), style = MaterialTheme.typography.bodyMedium, color = SubBlack)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(modifier = Modifier.size(100.dp).background(SubLightGray)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null, // 물결 애니메이션 제거
                        onClick = onLikeClicked
                    ),
                contentAlignment = Alignment.Center
            ) {
                // ★ isLiked 상태에 따라 보여줄 아이콘 리소스 분기 처리
                val likeIconRes = if (item.isLiked) {
                    R.drawable.btn_ddabong_pressed
                } else {
                    R.drawable.btn_ddabong_default
                }

                Icon(
                    painter = painterResource(likeIconRes),
                    contentDescription = "좋아요",
                    // ★ 아이콘 리소스 자체의 색상을 그대로 보여주기 위해 Unspecified 설정
                    tint = Color.Unspecified
                )
            }
        }
    }
}