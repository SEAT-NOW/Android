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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.MenuItemUiModel
import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.presentation.user.detail.StoreDetailViewModel
import java.text.DecimalFormat

@Composable
fun StoreDetailRoute(
    viewModel: StoreDetailViewModel = hiltViewModel(),
) {
    // ViewModel의 상태를 관찰 (State Hoisting)
    val storeDetail by viewModel.storeDetailState.collectAsState()
    val menuCategories by viewModel.menuListState.collectAsState()

    if (storeDetail != null) {
        StoreDetailScreen(
            storeDetail = storeDetail!!, // null-safety 보장
            menuCategories = menuCategories,
            onLikeClicked = viewModel::toggleMenuLike
        )
    } else {
        // 데이터 로딩 중 화면 (원하시는 로딩 뷰로 대체 가능)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PointRed)
        }
    }
}

fun formatPrice(price: Int): String {
    return DecimalFormat("#,###").format(price) + "원"
}

@Composable
fun StoreDetailScreen(
    storeDetail: StoreDetail,
    modifier: Modifier = Modifier,
    menuCategories: List<MenuCategoryUiModel>, // <- 추가됨
    initialTabIndex: Int = 0,
    onLikeClicked: (Long, Boolean) -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = White,
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = {
            // 하단 고정 Bar
            Surface(shadowElevation = 8.dp, color = White, modifier = Modifier.navigationBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_keep),
                            contentDescription = "킵하기",
                            tint = SubBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_link),
                            contentDescription = "공유하기",
                            tint = SubBlack,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(White)
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp)
        ) {
            // 1~4. 상단 정보 영역
            Text(text = "가게 상세페이지", style = Body1_Medium_14, fontWeight = FontWeight.Bold, color = SubBlack, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(contentPadding = PaddingValues(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (storeDetail.images.isEmpty()) {
                    item { Box(modifier = Modifier.width(265.dp).height(150.dp).background(SubPaleGray, RectangleShape)) }
                } else {
                    items(storeDetail.images) { Box(modifier = Modifier.width(265.dp).height(150.dp).background(SubLightGray, RectangleShape)) }
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

            // 5. 탭 영역
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

            // 6. 하단 탭 컨텐츠 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(vertical = 16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> StoreHomeContent(storeDetail = storeDetail)
                    1 -> {
                        StoreMenuContent(
                            menuCategories = menuCategories,
                            onLikeClicked = { menuId, isLiked ->
                                // 뷰모델의 좋아요 토글 함수 호출
                            }
                        )
                    }
                }
            }
        }
    }
}

// ★ 수정된 "홈" 탭 상세 정보 컴포넌트
@Composable
fun StoreHomeContent(storeDetail: StoreDetail) {
    // Divider와 LazyRow가 화면 끝까지 닿아야 하므로, 부모 Column에서는 패딩을 제거
    Column(modifier = Modifier.fillMaxWidth()) {

        // 1~4. 기존 텍스트 정보들 (여기에만 좌우 패딩 24dp 적용)
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

        // ★ 새로 추가된 Divider (가로 꽉 참)
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = SubLightGray // 또는 Divider LightGray 컬러
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ★ 위쪽과 동일한 스타일의 타이틀 텍스트
        Text(
            text = "사진", // 필요시 "가게 사진" 등으로 변경
            style = Body1_Medium_14,
            fontWeight = FontWeight.Bold,
            color = SubBlack,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ★ 새로 추가된 하단 사진 스크롤 영역
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp) // ★ 사진 간격 2dp
        ) {
            if (storeDetail.images.isEmpty()) {
                // 이미지가 없을 때의 Placeholder
                item {
                    Box(
                        modifier = Modifier
                            .width(118.dp)
                            .height(147.5.dp)
                            .background(SubPaleGray, RectangleShape) // 실제로는 Glide/Coil의 AsyncImage 사용
                    )
                }
            } else {
                // 이미지가 있을 때 (가로 118px, 세로 147.5px 비율 적용)
                items(storeDetail.images) { imageUrl ->
                    Box(
                        modifier = Modifier
                            .width(118.dp)
                            .height(147.5.dp)
                            .background(SubLightGray, RectangleShape) // 실제로는 AsyncImage 등으로 대체
                    )
                }
            }
        }
    }
}

// 아이콘 + 텍스트를 나열하는 공통 컴포저블 (기존과 동일)
@Composable
fun InfoRow(
    iconRes: Int,
    text: String,
    iconSize: Dp = 16.dp,
    iconOffsetX: Dp = 0.dp
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            Icon(painter = painterResource(iconRes), contentDescription = null, tint = SubGray, modifier = Modifier.size(iconSize).offset(x = iconOffsetX))
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = Body1_Medium_14, color = SubBlack)
    }
}

@Composable
fun StoreMenuContent(
    menuCategories: List<MenuCategoryUiModel>,
    onLikeClicked: (Long, Boolean) -> Unit
) {
    if (menuCategories.isEmpty() || menuCategories.all { it.menuItems.isEmpty() }) {
        // [데이터 없음 뷰]
        Column(
            modifier = Modifier.fillMaxSize().padding(start = 24.dp, top = 24.dp)
        ) {
            Text(
                text = "아직 메뉴 정보가 없어요",
                style = Body1_Medium_14,
                color = SubGray,
                fontWeight = FontWeight.Medium
            )
        }
    } else {
        // [메뉴 리스트 뷰]
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            menuCategories.forEachIndexed { index, category ->

                // 1. 구분선: 첫 번째 카테고리가 아닐 때만 표시
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        thickness = 1.dp,
                        color = SubLightGray
                    )
                }

                // ★ 2. 타이틀 상단 여백: 첫 번째 카테고리(index == 0)일 때만 top 여백을 0dp로 설정
                val topPadding = if (index == 0) 0.dp else 24.dp

                // 카테고리 헤더
                Text(
                    text = category.categoryName,
                    style = Body1_Medium_14,
                    color = SubGray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 24.dp, top = topPadding, bottom = 24.dp)
                )

                // 메뉴 아이템 렌더링
                category.menuItems.forEach { item ->
                    MenuItemView(
                        item = item,
                        onLikeClicked = { onLikeClicked(item.id, !item.isLiked) }
                    )
                }
            }
        }
    }
}
@Composable
fun MenuItemView(
    item: MenuItemUiModel,
    onLikeClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* 메뉴 상세 액션 */ }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // [왼쪽] 텍스트 정보
        Column(
            modifier = Modifier.weight(1f)
        ) {
            if (item.isRecommended) {
                Icon(
                    painter = painterResource(R.drawable.tag_normal), // TODO: 추천 태그 리소스로 교체
                    contentDescription = "추천",
                    tint = androidx.compose.ui.graphics.Color.Unspecified,
                    modifier = Modifier.height(24.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = item.name,
                style = Body1_Medium_14,
                fontWeight = FontWeight.Bold,
                color = SubBlack // 기존 테마 블랙
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = formatPrice(item.price),
                style = MaterialTheme.typography.bodyMedium,
                color = SubBlack // 기존 테마 블랙
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // [오른쪽] 정사각형 메뉴 사진 + 따봉 버튼
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(SubLightGray) // 기존 테마 라이트그레이
        ) {
            // TODO: 실제 이미지 연동 (AsyncImage 등)

            IconButton(
                onClick = onLikeClicked,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_keep), // TODO: 따봉 아이콘 리소스로 교체
                    contentDescription = "좋아요",
                    // 선택되면 기존 테마의 PointRed, 아니면 SubGray
                    tint = if (item.isLiked) PointRed else SubGray
                )
            }
        }
    }
}

// ★ Preview에서 여러 장의 사진이 추가된 상태를 확인할 수 있습니다.
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun StoreDetailScreenPreview() {
    SeatNowTheme {
        // 프리뷰용 메뉴 더미 데이터 생성
        val dummyMenuCategories = listOf(
            MenuCategoryUiModel(
                categoryName = "시그니처 메뉴",
                menuItems = listOf(
                    MenuItemUiModel(1, "바지락 술찜", 18000, "", true, false),
                    MenuItemUiModel(2, "매콤 국물 떡볶이", 15000, "", false, true)
                )
            )
        )

        StoreDetailScreen(
            storeDetail = StoreDetail(
                id = 1L,
                name = "맛있는 술집 신촌본점",
                images = listOf("image1", "image2", "image3", "image4"),
                operationStatus = "영업 중",
                availableSeatCount = 4,
                totalSeatCount = 15,
                status = StoreStatus.SPARE,
                universityInfo = "연세대학교 신촌캠퍼스 도보 5분",
                address = "서울특별시 서대문구 연세로 12길 34, 1층",
                openHours = "매일 17:00 ~ 03:00",
                closedDays = "토 · 일 휴무"
            ),
            // ★ 새로 추가된 파라미터 (상태와 이벤트 주입)
            menuCategories = dummyMenuCategories,
            onLikeClicked = { _, _ -> /* 프리뷰에서는 아무 동작 안 함 */ }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "1. 메뉴 있는 상태")
@Composable
fun StoreMenuContentPreview_Filled() {
    SeatNowTheme {
        // 더미 데이터 생성
        val dummyMenuCategories = listOf(
            MenuCategoryUiModel(
                categoryName = "시그니처 메뉴",
                menuItems = listOf(
                    MenuItemUiModel(
                        id = 1,
                        name = "바지락 술찜",
                        price = 18000,
                        imageUrl = "",
                        isRecommended = true,
                        isLiked = false
                    ),
                    MenuItemUiModel(
                        id = 2,
                        name = "매콤 국물 떡볶이",
                        price = 15000,
                        imageUrl = "",
                        isRecommended = false,
                        isLiked = true // 따봉 눌린 상태 (PointRed)
                    )
                )
            ),
            MenuCategoryUiModel(
                categoryName = "튀김/마른안주",
                menuItems = listOf(
                    MenuItemUiModel(
                        id = 3,
                        name = "모듬 감자튀김",
                        price = 12000,
                        imageUrl = "",
                        isRecommended = false,
                        isLiked = false
                    )
                )
            )
        )

        Box(modifier = Modifier.height(600.dp)) {
            StoreMenuContent(
                menuCategories = dummyMenuCategories,
                onLikeClicked = { _, _ -> }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "메뉴 탭 선택된 전체 화면")
@Composable
fun StoreDetailScreenMenuTabPreview() {
    SeatNowTheme {
        // 프리뷰용 메뉴 더미 데이터 (추천 있는 것 1개, 없는 것 1개)
        val dummyMenuCategories = listOf(
            MenuCategoryUiModel(
                categoryName = "시그니처 메뉴",
                menuItems = listOf(
                    MenuItemUiModel(
                        id = 1,
                        name = "바지락 술찜",
                        price = 18000,
                        imageUrl = "",
                        isRecommended = true,
                        isLiked = false
                    ),
                    MenuItemUiModel(
                        id = 2,
                        name = "매콤 국물 떡볶이",
                        price = 15000,
                        imageUrl = "",
                        isRecommended = false,
                        isLiked = true // 따봉 눌린 상태 (PointRed)
                    )
                )
            ),
            MenuCategoryUiModel(
                categoryName = "튀김/마른안주",
                menuItems = listOf(
                    MenuItemUiModel(
                        id = 3,
                        name = "모듬 감자튀김",
                        price = 12000,
                        imageUrl = "",
                        isRecommended = false,
                        isLiked = false
                    )
                )
            )
        )
        StoreDetailScreen(
            storeDetail = StoreDetail(
                id = 1L,
                name = "맛있는 술집 신촌본점",
                images = listOf("image1", "image2", "image3", "image4"),
                operationStatus = "영업 중",
                availableSeatCount = 4,
                totalSeatCount = 15,
                status = StoreStatus.SPARE,
                universityInfo = "연세대학교 신촌캠퍼스 도보 5분",
                address = "서울특별시 서대문구 연세로 12길 34, 1층",
                openHours = "매일 17:00 ~ 03:00",
                closedDays = "토 · 일 휴무"
            ),
            menuCategories = dummyMenuCategories,
            onLikeClicked = { _, _ -> },
            initialTabIndex = 1 // ★ 1번(메뉴) 탭이 눌린 상태로 렌더링
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "2. 데이터 없는 상태")
@Composable
fun StoreMenuContentPreview_Empty() {
    SeatNowTheme {
        Box(modifier = Modifier.height(300.dp)) {
            StoreMenuContent(
                menuCategories = emptyList(), // 빈 리스트 전달
                onLikeClicked = { _, _ -> }
            )
        }
    }
}