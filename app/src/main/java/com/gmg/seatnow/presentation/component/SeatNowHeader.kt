package com.gmg.seatnow.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.presentation.theme.Body1_Medium_14
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SubBlack
import com.gmg.seatnow.presentation.theme.SubLightGray
import com.gmg.seatnow.presentation.theme.SubPaleGray

@Composable
fun SeatNowDetailHeader(
    storeDetail: StoreDetail,
    modifier: Modifier = Modifier.Companion,
    customTopContent: @Composable () -> Unit = {},
    customBottomContent: @Composable () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 1. [Slot] 최상단 커스텀 영역 (ex. 타이틀 텍스트, 네비게이션 아이콘 등)
        customTopContent()

        Spacer(modifier = Modifier.Companion.height(16.dp))

        // 2. [공통] 상단 사진 스크롤 영역
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (storeDetail.images.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.Companion.width(265.dp).height(150.dp)
                            .background(SubPaleGray, RectangleShape)
                    )
                }
            } else {
                items(storeDetail.images) { imageUrl ->
                    // 추후 AsyncImage로 교체
                    Box(
                        modifier = Modifier.Companion.width(265.dp).height(150.dp)
                            .background(SubLightGray, RectangleShape)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.Companion.height(16.dp))

        // 3. [공통] 가게 이름
        Text(
            text = storeDetail.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Companion.Bold,
            color = SubBlack,
            modifier = Modifier.Companion.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.Companion.height(6.dp))

        // 4. [공통] 영업 상태 및 좌석 수 현황
        Row(
            modifier = Modifier.Companion.fillMaxWidth().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Text(
                text = storeDetail.operationStatus,
                style = Body1_Medium_14,
                fontWeight = FontWeight.Companion.Bold,
                color = SubBlack
            )
            Spacer(modifier = Modifier.Companion.weight(1f))
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = PointRed,
                            fontWeight = FontWeight.Companion.Bold
                        )
                    ) { append("${storeDetail.availableSeatCount}석") }
                    withStyle(
                        style = SpanStyle(
                            color = SubBlack,
                            fontWeight = FontWeight.Companion.Bold
                        )
                    ) { append(" / ${storeDetail.totalSeatCount}석") }
                },
                style = Body1_Medium_14
            )
            Spacer(modifier = Modifier.Companion.width(8.dp))

            val tagDrawableRes = when (storeDetail.status) {
                StoreStatus.SPARE -> R.drawable.tag_spare
                StoreStatus.NORMAL -> R.drawable.tag_normal
                StoreStatus.HARD -> R.drawable.tag_hard
                StoreStatus.FULL -> R.drawable.tag_full
            }
            Image(
                painter = painterResource(id = tagDrawableRes),
                contentDescription = null,
                modifier = Modifier.Companion.width(50.dp).height(24.dp),
                contentScale = ContentScale.Companion.Fit
            )
        }

        Spacer(modifier = Modifier.Companion.height(24.dp))

        // 5. [Slot] 하단 커스텀 영역 (탭바, 메뉴 정보 등 주입)
        customBottomContent()
    }
}