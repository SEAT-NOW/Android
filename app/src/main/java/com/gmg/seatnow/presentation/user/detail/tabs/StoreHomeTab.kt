package com.gmg.seatnow.presentation.user.detail.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.presentation.component.InfoRow // ★ 공통 컴포넌트 임포트

@Composable
fun StoreHomeTab(storeDetail: StoreDetail) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            // ★ SeatNowComponents.kt의 InfoRow 재사용
            InfoRow(iconRes = R.drawable.ic_school, text = storeDetail.universityInfo)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(iconRes = R.drawable.ic_itempin, text = storeDetail.address, iconSize = 18.dp)
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(iconRes = R.drawable.ic_clock, text = storeDetail.openHours)

            if (storeDetail.closedDays.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = storeDetail.closedDays,
                    style = Body1_Medium_14,
                    color = SubBlack,
                    modifier = Modifier.padding(start = 28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = SubLightGray)
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "사진",
            style = Body1_Medium_14,
            fontWeight = FontWeight.Bold,
            color = SubBlack,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (storeDetail.images.isEmpty()) {
                item { Box(modifier = Modifier.width(118.dp).height(147.5.dp).background(SubPaleGray, RectangleShape)) }
            } else {
                items(storeDetail.images) {
                    Box(modifier = Modifier.width(118.dp).height(147.5.dp).background(SubLightGray, RectangleShape))
                }
            }
        }
    }
}