package com.gmg.seatnow.presentation.owner.store.manage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.MenuItemUiModel
import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.presentation.owner.store.mypage.storeManage.StoreManagementViewModel
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.presentation.user.detail.tabs.StoreHomeTab
import com.gmg.seatnow.presentation.user.detail.tabs.StoreMenuTab

@Composable
fun StoreManagementScreen(
    viewModel: StoreManagementViewModel = hiltViewModel(),
    onEditStoreInfoClick: () -> Unit
) {
    val storeDetail by viewModel.storeDetailState.collectAsState()
    val menuCategories by viewModel.menuListState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStoreData()
    }

    if (storeDetail == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PointRed)
        }
    } else {
        StoreManagementContent(
            storeDetail = storeDetail!!,
            menuCategories = menuCategories,
            onEditStoreInfoClick = onEditStoreInfoClick
        )
    }
}

@Composable
fun StoreManagementContent(
    storeDetail: StoreDetail,
    menuCategories: List<MenuCategoryUiModel>,
    onEditStoreInfoClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("홈", "메뉴")

    Scaffold(
        containerColor = White,
        contentWindowInsets = WindowInsets.systemBars,
        // ★ [수정] BottomBar: 가게 정보 편집 버튼
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = White, modifier = Modifier.navigationBarsPadding()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    Button(
                        onClick = onEditStoreInfoClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PointRed,
                            contentColor = White
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "가게 정보 편집",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(White)
                .verticalScroll(scrollState)
                .padding(top = 16.dp)
        ) {
            // [UI 동일] 상단 텍스트
            Text(
                text = "가게 상세페이지",
                style = Body1_Medium_14,
                fontWeight = FontWeight.Bold,
                color = SubBlack,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // [UI 동일] 이미지 리스트
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 이미지가 없을 때 기본 박스 1개 표시
                if (storeDetail.images.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .width(265.dp)
                                .height(150.dp)
                                .background(SubLightGray, RectangleShape), // 배경색 변경
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_row_logo),
                                contentDescription = null,
                                tint = Color.White, // 로고 하얀색
                            )
                        }
                    }
                } else {
                    // 2. 이미지가 있을 때 (API 데이터 반영)
                    items(storeDetail.images) { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "매장 사진",
                            modifier = Modifier
                                .width(265.dp)
                                .height(150.dp)
                                .background(SubLightGray), // 로딩 중 배경색
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.ic_row_logo), // 로딩 중 아이콘
                            error = painterResource(id = R.drawable.ic_row_logo) // 에러 시 아이콘
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // [UI 동일] 가게 이름
            Text(
                text = storeDetail.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SubBlack,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))

            // [UI 동일] 영업 상태 및 좌석 정보
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = storeDetail.operationStatus, style = Body1_Medium_14, fontWeight = FontWeight.Bold, color = SubBlack)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = PointRed, fontWeight = FontWeight.Bold)) { append("${storeDetail.availableSeatCount}석") }
                        withStyle(style = SpanStyle(color = SubBlack, fontWeight = FontWeight.Bold)) { append(" / ${storeDetail.totalSeatCount}석") }
                    },
                    style = Body1_Medium_14
                )
                Spacer(modifier = Modifier.width(8.dp))
                val tagDrawableRes = when(storeDetail.status) {
                    StoreStatus.SPARE -> R.drawable.tag_spare
                    StoreStatus.NORMAL -> R.drawable.tag_normal
                    StoreStatus.HARD -> R.drawable.tag_hard
                    StoreStatus.FULL -> R.drawable.tag_full
                }
                Image(
                    painter = painterResource(id = tagDrawableRes),
                    contentDescription = null,
                    modifier = Modifier.width(50.dp).height(24.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // [UI 동일] 탭 레이아웃
            Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                tabTitles.forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { selectedTabIndex = index }
                            .drawBehind {
                                val strokeWidth = 1.dp.toPx()
                                if (isSelected) {
                                    drawLine(color = PointRed, start = Offset(0f, 0f), end = Offset(size.width, 0f), strokeWidth = strokeWidth)
                                } else {
                                    drawRect(color = SubLightGray, style = Stroke(width = strokeWidth))
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) PointRed else SubGray
                        )
                    }
                }
            }

            // [UI 동일] 탭 컨텐츠
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(vertical = 16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> StoreHomeTab(storeDetail = storeDetail)
                    1 -> {
                        // ★ 메뉴 데이터가 비어있는지 확인
                        if (menuCategories.isEmpty()) {
                            Text(
                                text = "메뉴를 입력해주세요.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = SubGray,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 20.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Start // 왼쪽 정렬
                            )
                        } else {
                            StoreMenuTab(
                                menuCategories = menuCategories,
                                onLikeClicked = { _, _ -> },
                                showLikeButton = false
                            )
                        }
                    }
                }
            }

            // 하단 버튼 여백 확보
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
fun PreviewStoreManagementScreen() {
    val dummyStore = StoreDetail(
        id = 1L,
        name = "맛있는 술집 신촌본점",
        images = emptyList(),
        operationStatus = "영업 중",
        storePhone = "02-1234-5678",
        availableSeatCount = 12,
        totalSeatCount = 50,
        status = StoreStatus.NORMAL,
        universityInfo = "연세대학교 신촌캠퍼스 도보 5분",
        address = "서울특별시 서대문구 연세로 12길 34",
        openHours = "매일 17:00 ~ 03:00",
        closedDays = "연중무휴",
        isKept = false
    )

    val dummyMenus = listOf(
        MenuCategoryUiModel(
            categoryName = "메인 메뉴",
            menuItems = listOf(
                MenuItemUiModel(1, "나가사키 짬뽕탕", 22000, "", true, false),
                MenuItemUiModel(2, "모듬 사시미 (대)", 35000, "", true, true)
            )
        )
    )

    SeatNowTheme {
        StoreManagementContent(
            storeDetail = dummyStore,
            menuCategories = dummyMenus,
            onEditStoreInfoClick = {}
        )
    }
}