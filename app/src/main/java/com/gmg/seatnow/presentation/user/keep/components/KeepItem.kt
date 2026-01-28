package com.gmg.seatnow.presentation.user.keep.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.presentation.user.keep.KeepStoreUiModel

@Composable
fun KeepItem(
    item: KeepStoreUiModel,
    onKeepClick: (KeepStoreUiModel) -> Unit,
    onItemClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(item.storeId) }
    ) {
        // 1. 이미지 영역 (308:180 비율)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(308f / 180f)
                .background(SubLightGray)
        ) {
            // TODO: 추후 Coil 라이브러리 등으로 실제 이미지 로드
            // AsyncImage( model = item.imageUrl, contentScale = ContentScale.Crop, ... )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. 가게 이름 + 상태 태그 + 킵 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 가게 이름
            Text(
                text = item.storeName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = SubBlack
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 상태 태그
            val tagRes = when (item.status) {
                StoreStatus.SPARE -> R.drawable.tag_spare
                StoreStatus.NORMAL -> R.drawable.tag_normal
                StoreStatus.HARD -> R.drawable.tag_hard
                StoreStatus.FULL -> R.drawable.tag_full
            }
            Image(
                painter = painterResource(id = tagRes),
                contentDescription = null,
                modifier = Modifier
                    .width(50.dp)
                    .height(24.dp),
                contentScale = ContentScale.Fit
            )

            // 이 Spacer가 남은 공간을 모두 차지하여 오른쪽으로 밀어냅니다.
            Spacer(modifier = Modifier.weight(1f))

            // ★ [수정됨] IconButton 제거 -> Icon에 직접 clickable 적용
            // 이렇게 하면 IconButton의 자체 패딩이 사라져서 완전히 오른쪽 끝에 붙습니다.
            val keepIconRes = if (item.isKept) R.drawable.ic_keep_pressed else R.drawable.ic_keep_default

            Icon(
                painter = painterResource(id = keepIconRes),
                contentDescription = "킵 취소",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // 리플 효과 제거 (원하시면 제거 안 해도 됩니다)
                    ) { onKeepClick(item) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. 대학명 + 좌석 정보
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_school),
                contentDescription = null,
                tint = SubGray,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = item.universityName,
                style = MaterialTheme.typography.bodyMedium,
                color = SubGray
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 좌석 수 표시
            Text(
                text = "${item.availableSeats}석 / ${item.totalSeats}석",
                style = MaterialTheme.typography.bodyMedium,
                color = SubGray
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "기본 상태")
@Composable
fun KeepItemPreview_Normal() {
    SeatNowTheme {
        KeepItem(
            item = KeepStoreUiModel(
                storeId = 1,
                storeName = "맛있는 술집 신촌본점",
                imageUrl = "",
                status = StoreStatus.NORMAL,
                universityName = "연세대학교",
                availableSeats = 4,
                totalSeats = 15,
                isKept = true
            ),
            onKeepClick = {},
            onItemClick = {}
        )
    }
}