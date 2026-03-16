package com.gmg.seatnow.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.presentation.theme.Body1_Medium_10
import com.gmg.seatnow.presentation.theme.Body1_Medium_14
import com.gmg.seatnow.presentation.theme.SubBlack
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.SubLightGray

@Composable
fun SeatNowMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.Companion,
    textColor: Color = SubBlack, // 기본 검정
    showArrow: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp), // 터치 영역 및 간격 확보
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Companion.Medium), // 스타일 통일
            color = textColor
        )

        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = SubLightGray,
                modifier = Modifier.Companion.size(24.dp)
            )
        }
    }
}

// 1. 리스트 아이템 컴포넌트
@Composable
fun StoreListItem(
    index: Int,
    store: Store,
    onItemClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Column(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .padding(vertical = 16.dp, horizontal = 24.dp)
    ) {
        // [상단 정보]
        Row(
            modifier = Modifier.Companion.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Text(
                text = "${index}. ${store.name}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Companion.Bold,
                    fontSize = 18.sp
                ),
                color = SubBlack
            )

            val tagResId = when (store.status) {
                StoreStatus.FULL -> R.drawable.tag_full
                StoreStatus.HARD -> R.drawable.tag_hard
                StoreStatus.NORMAL -> R.drawable.tag_normal
                else -> R.drawable.tag_spare
            }

            Image(
                painter = painterResource(id = tagResId),
                contentDescription = store.status.name,
                modifier = Modifier.Companion.height(22.dp),
                contentScale = ContentScale.Companion.Fit
            )
        }

        Spacer(modifier = Modifier.Companion.height(6.dp))

        // [중간 정보]
        Row(verticalAlignment = Alignment.Companion.CenterVertically) {
            Text(
                text = store.operationStatus,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Companion.Medium),
                color = SubBlack
            )
            Spacer(modifier = Modifier.Companion.width(3.dp))
            Text(
                text = "·",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Companion.Bold),
                color = SubGray
            )
            Spacer(modifier = Modifier.Companion.width(3.dp))
            Icon(
                painter = painterResource(R.drawable.ic_itempin),
                contentDescription = null,
                tint = SubGray,
                modifier = Modifier.Companion.size(10.dp)
            )
            Spacer(modifier = Modifier.Companion.width(6.dp))
            Text(
                text = store.neighborhood,
                style = MaterialTheme.typography.bodySmall,
                color = SubGray
            )
            if (store.distance.isNotBlank()) {
                Text(
                    text = "  ${store.distance}",
                    style = Body1_Medium_10,
                    color = SubLightGray
                )
            }
            Spacer(modifier = Modifier.Companion.weight(1f))
            Icon(
                painter = painterResource(R.drawable.btn_calling),
                contentDescription = "전화 걸기",
                tint = SubGray,
                modifier = Modifier.Companion.size(12.dp).clickable { onCallClick() }
            )
        }

        Spacer(modifier = Modifier.Companion.height(12.dp))

        // [하단 사진] ★ Rounded 제거, 높이 90dp, 직각
        DynamicStoreImageRow(
            images = store.images,
            modifier = Modifier.Companion.height(90.dp),
            shape = RectangleShape, // ★ 무조건 직각
            spacing = 2.dp
        )
    }
}


// 2. 상세 카드 컴포넌트
@Composable
fun StoreDetailCard(
    index: Int,
    store: Store,
    onItemClick: () -> Unit = {},
    onCallClick: () -> Unit
) {
    Surface(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp), // 카드 테두리는 둥글게 (요청 사항 외 유지)
        color = Color.Companion.White,
        tonalElevation = 0.dp,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .clickable(onClick = onItemClick)
        ) {
            // [상단 사진] ★ 상세 카드는 높이 120dp, 직각
            DynamicStoreImageRow(
                images = store.images,
                modifier = Modifier.Companion.height(120.dp),
                shape = RectangleShape, // ★ 무조건 직각 (상단 뚜껑 역할)
                spacing = 2.dp
            )

            // [하단 텍스트 정보]
            Column(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    Text(
                        text = "${index}. ${store.name}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Companion.Bold,
                            fontSize = 20.sp
                        ),
                        color = SubBlack
                    )
                    val tagResId = when (store.status) {
                        StoreStatus.FULL -> R.drawable.tag_full
                        StoreStatus.HARD -> R.drawable.tag_hard
                        StoreStatus.NORMAL -> R.drawable.tag_normal
                        else -> R.drawable.tag_spare
                    }
                    Image(
                        painter = painterResource(id = tagResId),
                        contentDescription = store.status.name,
                        modifier = Modifier.Companion.height(24.dp),
                        contentScale = ContentScale.Companion.Fit
                    )
                }
                Spacer(modifier = Modifier.Companion.height(8.dp))
                Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                    Text(
                        text = store.operationStatus,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Companion.Medium),
                        color = SubBlack
                    )
                    Spacer(modifier = Modifier.Companion.width(3.dp))
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Companion.Bold),
                        color = SubGray
                    )
                    Spacer(modifier = Modifier.Companion.width(3.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_itempin),
                        contentDescription = null,
                        tint = SubGray,
                        modifier = Modifier.Companion.size(10.dp)
                    )
                    Spacer(modifier = Modifier.Companion.width(6.dp))
                    Text(
                        text = store.neighborhood,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubGray
                    )
                    if (store.distance.isNotBlank()) {
                        Text(
                            text = "  ${store.distance}",
                            style = Body1_Medium_10,
                            color = SubGray
                        )
                    }
                    Spacer(modifier = Modifier.Companion.weight(1f))
                    Icon(
                        painter = painterResource(R.drawable.btn_calling),
                        contentDescription = "전화 걸기",
                        tint = SubGray,
                        modifier = Modifier.Companion.size(12.dp).clickable { onCallClick() }
                    )
                }
            }
        }
    }
}

/**
 * [아이콘 + 텍스트 공통 컴포넌트]
 * - 상세 페이지, 검색 결과 등 여러 곳에서 재사용
 */
@Composable
fun InfoRow(
    iconRes: Int,
    text: String,
    iconSize: Dp = 16.dp,
    iconOffsetX: Dp = 0.dp
) {
    // ★ 수정: verticalAlignment를 CenterVertically -> Top 으로 변경
    Row(
        modifier = Modifier.Companion.fillMaxWidth(),
        verticalAlignment = Alignment.Companion.Top
    ) {
        // 아이콘 박스 (24dp)
        // Top 정렬을 했기 때문에 이 박스는 텍스트의 첫 줄과 높이를 나란히 하게 됩니다.
        Box(
            modifier = Modifier.Companion.size(24.dp).offset(y = (-2.5).dp),
            contentAlignment = Alignment.Companion.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = SubGray,
                modifier = Modifier.Companion
                    .size(iconSize)
                    .offset(x = iconOffsetX)
            )
        }

        Spacer(modifier = Modifier.Companion.width(4.dp))

        // 텍스트
        // 만약 텍스트 첫 줄과 아이콘의 미세한 높이가 안 맞는다면
        // 여기에 Modifier.padding(top = 1.dp) 등으로 미세 조정을 할 수 있습니다.
        // 하지만 보통 14sp 텍스트와 24dp 아이콘 박스는 Top 정렬 시 잘 맞습니다.
        Text(
            text = text,
            style = Body1_Medium_14,
            color = SubBlack
        )
    }
}