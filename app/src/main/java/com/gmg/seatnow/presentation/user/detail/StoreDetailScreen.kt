package com.gmg.seatnow.presentation.user.detail

import android.widget.Toast
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.MenuItemUiModel
import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.presentation.user.detail.tabs.StoreHomeTab
import com.gmg.seatnow.presentation.user.detail.tabs.StoreMenuTab
import com.gmg.seatnow.presentation.util.IntentUtil
import kotlinx.coroutines.flow.collectLatest

@Composable
fun StoreDetailRoute(
    viewModel: StoreDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val storeDetail by viewModel.storeDetailState.collectAsState()
    val menuCategories by viewModel.menuListState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is StoreDetailViewModel.UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is StoreDetailViewModel.UiEvent.NavigateBack -> {
                    onBackClick() // 데이터 로드 실패 시 화면 종료
                }
            }
        }
    }

    if (storeDetail != null) {
        StoreDetailScreen(
            storeDetail = storeDetail!!,
            menuCategories = menuCategories,
            onLikeClicked = viewModel::onLikeClicked,
            onKeepClicked = viewModel::onKeepClicked, // ★ ViewModel 함수 연결
            onBackClick = onBackClick
        )
    } else {
        // 로딩 중 UI
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PointRed)
        }
    }
}


@Composable
fun StoreDetailScreen(
    storeDetail: StoreDetail,
    modifier: Modifier = Modifier,
    menuCategories: List<MenuCategoryUiModel>,
    initialTabIndex: Int = 0,
    onLikeClicked: (Long) -> Unit,
    onKeepClicked: (Long, Boolean) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current // ★ 전화 걸기를 위한 Context

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = White,
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = White, modifier = Modifier.navigationBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onKeepClicked(storeDetail.id, !storeDetail.isKept) }) {
                        // isKept 상태에 따라 보여줄 아이콘 리소스 결정
                        val keepIconRes = if (storeDetail.isKept) {
                            // TODO: 여기에 '눌렸을 때(빨간색/채워진)' 사용할 Drawable ID를 넣으세요.
                            R.drawable.ic_keep_pressed // (예시: R.drawable.ic_keep_pressed)
                        } else {
                            // 평상시(안 눌렸을 때) 사용할 Drawable ID
                            R.drawable.ic_keep_default
                        }

                        Icon(
                            painter = painterResource(keepIconRes),
                            contentDescription = "킵하기",
                            // ★ Drawable 본연의 색상(빨강/검정 등)을 그대로 쓰기 위해 Unspecified 설정
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = {
                        IntentUtil.shareStoreLink(
                            context = context,
                            storeId = storeDetail.id,
                            storeName = storeDetail.name
                        )
                    }) {
                        Icon(painter = painterResource(R.drawable.ic_link), contentDescription = "공유하기", tint = SubBlack, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { IntentUtil.makePhoneCall(context, storeDetail.storePhone) },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PointRed)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painter = painterResource(R.drawable.ic_button_calling), contentDescription = "전화 걸기", tint = White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("전화 문의하기", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = White)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).background(White).verticalScroll(rememberScrollState()).padding(top = 16.dp)
        ) {
            Text(text = "가게 상세페이지", style = Body1_Medium_14, fontWeight = FontWeight.Bold, color = SubBlack, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {                if (storeDetail.images.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.width(265.dp).height(150.dp).background(SubLightGray, RectangleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_row_logo),
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                } else {
                // ★ 실제 이미지 로드 (AsyncImage 사용)
                items(storeDetail.images) { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "매장 사진",
                        modifier = Modifier
                            .width(265.dp)
                            .height(150.dp)
                            .background(SubLightGray, RectangleShape),
                        contentScale = ContentScale.Crop, // 이미지 크롭 처리
                        placeholder = painterResource(id = R.drawable.ic_row_logo), // 로딩 중 표시
                        error = painterResource(id = R.drawable.ic_row_logo) // 에러 시 표시
                    )
                }
            }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = storeDetail.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = SubBlack, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
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
                Image(painter = painterResource(id = tagDrawableRes), contentDescription = null, modifier = Modifier.width(50.dp).height(24.dp), contentScale = ContentScale.Fit)
            }

            Spacer(modifier = Modifier.height(24.dp))

            var selectedTabIndex by remember { mutableStateOf(initialTabIndex) }
            val tabTitles = listOf("홈", "메뉴")

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
                        Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = if (isSelected) PointRed else SubGray)
                    }
                }
            }

            // ★ 6. 하단 탭 컨텐츠 영역 (분리한 파일을 호출)
            Box(modifier = Modifier.fillMaxWidth().background(White).padding(vertical = 16.dp)) {
                when (selectedTabIndex) {
                    0 -> StoreHomeTab(storeDetail = storeDetail)
                    1 -> StoreMenuTab(menuCategories = menuCategories, onLikeClicked = onLikeClicked)
                }
            }
        }
    }
}

// ======================= PREVIEWS =======================

//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//@Composable
//fun StoreDetailScreenPreview() {
//    SeatNowTheme {
//        val dummyMenuCategories = listOf(
//            MenuCategoryUiModel(
//                categoryName = "시그니처 메뉴",
//                menuItems = listOf(
//                    MenuItemUiModel(1, "바지락 술찜", 18000, "", true, false),
//                    MenuItemUiModel(2, "매콤 국물 떡볶이", 15000, "", false, true)
//                )
//            )
//        )
//
//        StoreDetailScreen(
//            storeDetail = StoreDetail(
//                id = 1L,
//                name = "맛있는 술집 신촌본점",
//                images = listOf("image1", "image2", "image3", "image4"),
//                operationStatus = "영업 중",
//                storePhone = "02-312-3456",
//                availableSeatCount = 4,
//                totalSeatCount = 15,
//                status = StoreStatus.SPARE,
//                universityInfo = "연세대학교 신촌캠퍼스 도보 5분",
//                address = "서울특별시 서대문구 연세로 12길 34, 1층",
//                openHours = "매일 17:00 ~ 03:00",
//                closedDays = "토 · 일 휴무"
//            ),
//            menuCategories = dummyMenuCategories,
//            onLikeClicked = { _, _ -> },
//            onKeepClicked = {},
//            onBackClick = {}
//        )
//    }
//}

//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "1. 메뉴 있는 상태")
//@Composable
//fun StoreMenuTabPreview_Filled() {
//    SeatNowTheme {
//        val dummyMenuCategories = listOf(
//            MenuCategoryUiModel(
//                categoryName = "시그니처 메뉴",
//                menuItems = listOf(
//                    MenuItemUiModel(id = 1, name = "바지락 술찜", price = 18000, imageUrl = "", isRecommended = true, isLiked = false),
//                    MenuItemUiModel(id = 2, name = "매콤 국물 떡볶이", price = 15000, imageUrl = "", isRecommended = false, isLiked = true)
//                )
//            ),
//            MenuCategoryUiModel(
//                categoryName = "튀김/마른안주",
//                menuItems = listOf(
//                    MenuItemUiModel(id = 3, name = "모듬 감자튀김", price = 12000, imageUrl = "", isRecommended = false, isLiked = false)
//                )
//            )
//        )
//
//        Box(modifier = Modifier.height(600.dp)) {
//            // ★ 수정됨: StoreMenuContent -> StoreMenuTab
//            StoreMenuTab(
//                menuCategories = dummyMenuCategories,
//                onLikeClicked = { _, _ -> }
//            )
//        }
//    }
//}

//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "메뉴 탭 선택된 전체 화면")
//@Composable
//fun StoreDetailScreenMenuTabPreview() {
//    SeatNowTheme {
//        val dummyMenuCategories = listOf(
//            MenuCategoryUiModel(
//                categoryName = "시그니처 메뉴",
//                menuItems = listOf(
//                    MenuItemUiModel(id = 1, name = "바지락 술찜", price = 18000, imageUrl = "", isRecommended = true, isLiked = false),
//                    MenuItemUiModel(id = 2, name = "매콤 국물 떡볶이", price = 15000, imageUrl = "", isRecommended = false, isLiked = true)
//                )
//            ),
//            MenuCategoryUiModel(
//                categoryName = "튀김/마른안주",
//                menuItems = listOf(
//                    MenuItemUiModel(id = 3, name = "모듬 감자튀김", price = 12000, imageUrl = "", isRecommended = false, isLiked = false)
//                )
//            )
//        )
//        StoreDetailScreen(
//            storeDetail = StoreDetail(
//                id = 1L,
//                name = "맛있는 술집 신촌본점",
//                images = listOf("image1", "image2", "image3", "image4"),
//                operationStatus = "영업 중",
//                storePhone = "02-312-3456",
//                availableSeatCount = 4,
//                totalSeatCount = 15,
//                status = StoreStatus.SPARE,
//                universityInfo = "연세대학교 신촌캠퍼스 도보 5분",
//                address = "서울특별시 서대문구 연세로 12길 34, 1층",
//                openHours = "매일 17:00 ~ 03:00",
//                closedDays = "토 · 일 휴무"
//            ),
//            menuCategories = dummyMenuCategories,
//            onLikeClicked = { _, _ -> },
//            onKeepClicked = {},
//            onBackClick = {},
//            initialTabIndex = 1
//        )
//    }
//}

//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "2. 데이터 없는 상태")
//@Composable
//fun StoreMenuTabPreview_Empty() {
//    SeatNowTheme {
//        Box(modifier = Modifier.height(300.dp)) {
//            // ★ 수정됨: StoreMenuContent -> StoreMenuTab
//            StoreMenuTab(
//                menuCategories = emptyList(),
//                onLikeClicked = { _, _ -> }
//            )
//        }
//    }
//}