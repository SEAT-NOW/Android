package com.gmg.seatnow.presentation.component

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.gmg.seatnow.R
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SubBlack
import com.gmg.seatnow.presentation.theme.SubLightGray
import com.gmg.seatnow.presentation.theme.SubPaleGray
import com.gmg.seatnow.presentation.theme.White

// [Step 5] 등록된 사진 아이템 (X버튼 작게, 여백 축소)
@Composable
fun PhotoGridItem(
    uri: Uri,
    isRepresentative: Boolean,
    onRemove: () -> Unit,
    onSetRepresentative: () -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    Box(modifier = modifier) {
        val interactionSource = remember { MutableInteractionSource() }

        // 1. 실제 사진 영역
        Box(
            modifier = Modifier.Companion
                // [수정] X버튼 공간 확보용 패딩 10dp -> 6dp로 축소 (사진 크기 확보)
                .padding(top = 6.dp, end = 6.dp)
                .fillMaxWidth()
                .aspectRatio(4f / 5f)
                .background(SubLightGray, RectangleShape)
                .border(1.dp, SubLightGray, RectangleShape)
                .clickable(onClick = onSetRepresentative)
        ) {
            AsyncImage(
                model = uri,
                contentDescription = "가게 사진",
                modifier = Modifier.Companion.fillMaxSize(),
                contentScale = ContentScale.Companion.Crop
            )

            // 2. 대표 라벨
            val labelBgColor = if (isRepresentative) PointRed else SubLightGray
            val labelTextColor = White

            Box(
                modifier = Modifier.Companion
                    .background(labelBgColor, RectangleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.Companion.TopStart)
            ) {
                Text(
                    text = "대표",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = labelTextColor,
                    fontWeight = FontWeight.Companion.Bold
                )
            }
        }

        // 3. 삭제 버튼 (크기 축소)
        Box(
            modifier = Modifier.Companion
                .align(Alignment.Companion.TopEnd)
                // [수정] 버튼 크기 20dp -> 18dp로 축소
                .size(18.dp)
                .background(SubBlack, CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null, // 여기가 null이면 물결이 안 생깁니다.
                    onClick = onRemove
                ),
            contentAlignment = Alignment.Companion.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "삭제",
                tint = White,
                // [수정] 아이콘 크기 12dp -> 10dp로 축소
                modifier = Modifier.Companion.size(10.dp)
            )
        }
    }
}

@Composable
fun DynamicStoreImageRow(
    images: List<String>,
    modifier: Modifier = Modifier.Companion,
    shape: Shape = RectangleShape, // 기본값 직각
    spacing: Dp = 2.dp
) {
    // 1. 이미지가 없는 경우 (Default 이미지로 꽉 채움)
    if (images.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape) // RectangleShape
                .background(SubLightGray),
            contentAlignment = Alignment.Companion.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_row_logo),
                contentDescription = null,
                tint = Color.Companion.White,
                modifier = Modifier.Companion.size(75.dp)
            )
        }
    } else {
        // 2. 이미지가 있는 경우 (1장, 2장, 3장)
        // 최대 3장까지만 가져옴
        val displayImages = images.take(3)

        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape) // RectangleShape
        ) {
            displayImages.forEachIndexed { index, imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "가게 이미지",
                    contentScale = ContentScale.Companion.Crop,
                    modifier = Modifier.Companion
                        .weight(1f) // ★ 1개면 100%, 2개면 50%씩, 3개면 33%씩 꽉 채움
                        .fillMaxHeight()
                        .background(SubLightGray)
                )

                // 마지막 이미지가 아니라면 간격 추가
                if (index < displayImages.lastIndex) {
                    Spacer(modifier = Modifier.Companion.width(spacing))
                }
            }
        }
    }
}


@Composable
fun FixedThreeImagesRow(
    images: List<String>,
    shape: Shape = RectangleShape, // 기본값: 뾰족하게
    spacing: Dp = 8.dp,            // 기본값: 간격 8dp
    modifier: Modifier = Modifier.Companion
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing)
    ) {
        for (i in 0 until 3) {
            Box(
                modifier = Modifier.Companion
                    .weight(1f)
                    .aspectRatio(1f)
                    .clip(shape) // ★ shape 적용 (ListItem은 Rectangle, Detail은 CardClip)
                    .background(SubPaleGray)
            ) {
                if (i < images.size) {
                    AsyncImage(
                        model = images[i],
                        contentDescription = "가게 사진",
                        modifier = Modifier.Companion.fillMaxSize(),
                        contentScale = ContentScale.Companion.Crop
                    )
                }
            }
        }
    }
}