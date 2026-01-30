package com.gmg.seatnow.presentation.user.detail.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.presentation.component.InfoRow

@Composable
fun StoreHomeTab(storeDetail: StoreDetail) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
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
            // 비율 상수 정의 (118 / 147.5 = 0.8)
            val imageAspectRatio = 0.8f
            // 아이콘이 박스 내에서 차지할 비율 (0.3 = 30%)
            val iconScaleFraction = 0.8f

            if (storeDetail.images.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .height(147.5.dp) // 기준 높이만 설정
                            .aspectRatio(imageAspectRatio) // 비율에 맞춰 너비 자동 설정
                            .background(SubPaleGray, RectangleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_row_logo),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .fillMaxSize(iconScaleFraction) // 부모 크기의 30%만큼 채움 (비율 유지)
                                .aspectRatio(1f) // 아이콘 1:1 비율 유지
                        )
                    }
                }
            } else {
                items(storeDetail.images) {
                    Box(
                        modifier = Modifier
                            .height(147.5.dp) // 기준 높이
                            .aspectRatio(imageAspectRatio) // 4:5 비율 유지 (너비 자동)
                            .background(SubLightGray, RectangleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_row_logo),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .fillMaxSize(iconScaleFraction) // 부모 크기에 비례하여 사이즈 자동 조절
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}