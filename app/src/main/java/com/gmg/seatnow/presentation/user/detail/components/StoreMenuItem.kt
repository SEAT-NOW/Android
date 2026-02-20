package com.gmg.seatnow.presentation.user.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape // 추가
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // 추가
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale // 추가
import androidx.compose.ui.platform.LocalContext // 추가
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage // 추가
import coil.request.ImageRequest // 추가
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.MenuItemUiModel
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.presentation.util.IntentUtil

@Composable
fun StoreMenuItem(
    item: MenuItemUiModel,
    onLikeClicked: () -> Unit,
    showLikeButton: Boolean = true
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

        // ★★★ [수정된 부분] 이미지 박스 ★★★
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(SubLightGray),
            contentAlignment = Alignment.Center
        ) {
            // 1. 이미지가 있으면 그리기 (기존 코드엔 이 부분이 아예 없었습니다!)
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "메뉴 이미지",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(id = R.drawable.ic_row_logo), // 에러시 로고
                    placeholder = painterResource(id = R.drawable.ic_row_logo) // 로딩시 로고
                )
            } else {
                // 2. 없으면 로고 아이콘 표시
                Icon(
                    painter = painterResource(id = R.drawable.ic_row_logo),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // 3. 좋아요 버튼 (이미지 위에 뜸)
            if (showLikeButton) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onLikeClicked
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val likeIconRes = if (item.isLiked) {
                        R.drawable.btn_ddabong_pressed
                    } else {
                        R.drawable.btn_ddabong_default
                    }

                    Icon(
                        painter = painterResource(likeIconRes),
                        contentDescription = "좋아요",
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}