package com.gmg.seatnow.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmg.seatnow.R
import com.gmg.seatnow.presentation.theme.ColorSearchTag
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SubBlack
import com.gmg.seatnow.presentation.theme.SubDarkGray
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.White

// [1] 상단 검색바 (홈/검색 공통 사용)
@Composable
fun HomeSearchBar(
    activeHeadCount: Int?,
    onClearFilter: () -> Unit,
    onSearchClick: () -> Unit
) {

    Surface(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .height(52.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .clickable(onClick = onSearchClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        color = White
    ) {
        Row(
            verticalAlignment = Alignment.Companion.CenterVertically,
            modifier = Modifier.Companion.padding(horizontal = 16.dp)
        ) {
            // 1. 왼쪽: 돋보기 아이콘 (항상 고정)
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "검색",
                tint = PointRed,
                modifier = Modifier.Companion.size(24.dp)
            )

            Spacer(modifier = Modifier.Companion.width(12.dp))

            // 2. 중앙: 칩 또는 힌트 텍스트 (weight 1f로 공간 차지)
            Box(
                modifier = Modifier.Companion.weight(1f),
                contentAlignment = Alignment.Companion.CenterStart
            ) {
                if (activeHeadCount != null) {
                    // [필터 상태] : 노란색 칩 표시 ("4명 자리") & 힌트 텍스트 숨김
                    Surface(
                        color = ColorSearchTag,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp), // 둥근 모서리
                        modifier = Modifier.Companion.height(30.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Companion.Center,
                            modifier = Modifier.Companion.padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = "${activeHeadCount}명 자리",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Companion.Bold,
                                    fontSize = 13.sp
                                ),
                                color = SubBlack
                            )
                        }
                    }
                } else {
                    // [기본 상태] : 힌트 텍스트 표시
                    Text(
//                        text = "장소, 지역, 대학명 검색",
                        text = "N명 자리찾기를 시작하세요!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubGray
                    )
                }
            }

            // 3. 오른쪽: X 버튼 (필터가 있을 때만 맨 오른쪽에 표시)
            if (activeHeadCount != null) {
                // 터치 영역 확보를 위해 Box 사용 권장
                Box(
                    modifier = Modifier.Companion
                        .size(32.dp)
                        .clickable(onClick = onClearFilter),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    // 사진처럼 회색 동그라미 배경의 X 아이콘을 원하시면 수정 가능,
                    // 여기선 깔끔한 아이콘으로 구현
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "필터 삭제",
                        tint = SubGray,
                        modifier = Modifier.Companion.size(20.dp)
                    )
                }
            }
        }
    }
}

// [2] 현 지도에서 검색 버튼
@Composable
fun SearchHereButton(
    modifier: Modifier = Modifier.Companion,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
        color = White,
        shadowElevation = 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.Companion.CenterVertically,
            modifier = Modifier.Companion.padding(horizontal = 12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.Companion.size(16.dp),
                    color = PointRed,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.Companion.width(6.dp))
                Text(
                    text = "검색 중...",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Companion.Bold),
                    color = PointRed
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = PointRed,
                    modifier = Modifier.Companion.size(16.dp)
                )
                Spacer(modifier = Modifier.Companion.width(6.dp))
                Text(
                    text = "현 지도에서 검색",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Companion.Bold),
                    color = PointRed
                )
            }
        }
    }
}

// [3] 현재 위치 버튼
@Composable
fun CurrentLocationButton(
    modifier: Modifier = Modifier.Companion,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconColor = if (isSelected) PointRed else SubDarkGray

    Box(
        modifier = modifier
            .size(48.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Companion.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_current),
            contentDescription = "현재 위치",
            tint = iconColor
        )
    }
}