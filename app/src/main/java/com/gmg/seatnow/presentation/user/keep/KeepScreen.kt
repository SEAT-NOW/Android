package com.gmg.seatnow.presentation.user.keep

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.presentation.user.keep.components.KeepItem

// [Stateful] ViewModel과 연결되는 컴포저블
@Composable
fun KeepScreen(
    viewModel: KeepViewModel = hiltViewModel(),
    onNavigateToDetail: (Long) -> Unit = {}
) {
    val keepList by viewModel.keepList.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchKeepList()
    }

    // UI를 그리는 Stateless 컴포저블 호출
    KeepScreenContent(
        keepList = keepList,
        onToggleKeep = { item -> viewModel.toggleKeep(item) },
        onNavigateToDetail = onNavigateToDetail
    )
}

// [Stateless] 실제 UI를 그리는 컴포저블 (프리뷰 용이)
@Composable
fun KeepScreenContent(
    keepList: List<KeepStoreUiModel>,
    onToggleKeep: (KeepStoreUiModel) -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        // [1] 상단 고정 타이틀
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 20.dp)
        ) {
            Text(
                text = "킵",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = SubBlack,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // [2] 컨텐츠 영역
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (keepList.isEmpty()) {
                // [빈 화면 UI]
                KeepEmptyView()
            } else {
                // [리스트 화면 UI]
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(keepList) { item ->
                        KeepItem(
                            item = item,
                            onKeepClick = { onToggleKeep(item) },
                            onItemClick = { onNavigateToDetail(item.storeId) }
                        )
                    }
                }
            }
        }
    }
}

// 빈 화면 UI 별도 분리 (재사용성 및 가독성)
@Composable
fun KeepEmptyView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(bottom = 60.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_keep_default),
            contentDescription = null,
            modifier = Modifier.size(70.dp),
            colorFilter = ColorFilter.tint(SubDarkGray)
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "자주 찾는 술집을 킵해보세요",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = SubDarkGray
            )
        )
    }
}


// ================= PREVIEWS =================

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "화면 - 데이터 있음")
@Composable
fun KeepScreenContent_List_Preview() {
    SeatNowTheme {
        // 더미 데이터 생성
        val mockData = listOf(
            KeepStoreUiModel(1, "맛있는 술집 신촌본점", "", StoreStatus.SPARE, "연세대학교", 4, 15, true),
            KeepStoreUiModel(2, "포차천국 홍대점", "", StoreStatus.FULL, "홍익대학교", 0, 30, true)
        )

        KeepScreenContent(
            keepList = mockData,
            onToggleKeep = {},
            onNavigateToDetail = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "화면 - 데이터 없음(빈 화면)")
@Composable
fun KeepScreenContent_Empty_Preview() {
    SeatNowTheme {
        KeepScreenContent(
            keepList = emptyList(), // 빈 리스트 전달
            onToggleKeep = {},
            onNavigateToDetail = {}
        )
    }
}