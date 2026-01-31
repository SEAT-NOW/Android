package com.gmg.seatnow.presentation.owner.store.mypage.storeManage.storeManageEdit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.StoreMenuCategory
import com.gmg.seatnow.domain.model.StoreMenuItemData
import com.gmg.seatnow.presentation.component.SeatNowRedPlusButton
import com.gmg.seatnow.presentation.theme.*

@Composable
fun TabContentMenu(
    viewModel: StoreEditMainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(White),
        contentPadding = PaddingValues(24.dp)
    ) {
        items(uiState.menuCategories) { category ->
            MenuCategoryItem(
                category = category,
                onAddMenuItemClick = {
                    viewModel.onAction(StoreEditAction.OpenAddMenu(category.id))
                },
                onCategoryEditClick = {
                    viewModel.onAction(StoreEditAction.SetCategoryEditMode(true))
                },
                onMoveItem = { from, to ->
                    viewModel.onAction(StoreEditAction.MoveMenuItem(category.id, from, to))
                },
                onMenuItemClick = { item ->
                    viewModel.onAction(StoreEditAction.OpenEditMenu(category.id, item))
                }
            )
            Spacer(modifier = Modifier.height(40.dp))
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun MenuCategoryItem(
    category: StoreMenuCategory,
    onAddMenuItemClick: () -> Unit,
    onCategoryEditClick: () -> Unit,
    onMoveItem: (Int, Int) -> Unit,
    onMenuItemClick: (StoreMenuItemData) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. 카테고리 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCategoryEditClick
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                color = SubBlack
            )

            Icon(
                painter = painterResource(id = R.drawable.btn_menuedit),
                contentDescription = "카테고리 수정",
                modifier = Modifier.size(15.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 메뉴 아이템 리스트
        // ★ [중요] 여기는 LazyColumn이 아닌 일반 Column이므로 animateItem()은 사용할 수 없지만,
        // EditStoreMenuItem 내부의 그래픽 레이어 조작으로 드래그 효과를 구현합니다.
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            category.items.forEachIndexed { index, item ->
                // key를 사용하여 컴포지션 효율 최적화
                key(item.id) {
                    EditStoreMenuItem(
                        item = item,
                        index = index,
                        totalCount = category.items.size,
                        onMove = onMoveItem,
                        onClick = { onMenuItemClick(item) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. 추가 버튼
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            SeatNowRedPlusButton(
                onClick = onAddMenuItemClick,
                isEnabled = true
            )
        }
    }
}

@Composable
fun EditStoreMenuItem(
    item: StoreMenuItemData,
    index: Int,
    totalCount: Int,
    onMove: (Int, Int) -> Unit,
    onClick: () -> Unit
) {
    // ★ [핵심 1] 드래그 로직 및 상태 변수 (CategoryEditScreen과 동일)
    val currentIndex by rememberUpdatedState(index)

    var itemHeightPx by remember { mutableIntStateOf(0) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    // ★ [핵심 2] 간격 보정: 여기는 spacedBy(12.dp) 이므로 12dp로 계산
    val spacingPx = with(density) { 12.dp.toPx() }

    // ★ [핵심 3] 시각적 피드백 애니메이션
    val targetScale by animateFloatAsState(targetValue = if (isDragging) 1.05f else 1f, label = "scale")
    val targetAlpha by animateFloatAsState(targetValue = if (isDragging) 0.7f else 1f, label = "alpha")
    val targetElevation = if (isDragging) 8.dp else 0.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 드래그 중일 때 다른 아이템 위로 올라오도록 zIndex 설정
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                translationY = dragOffset // 손가락 따라 이동
                scaleX = targetScale
                scaleY = targetScale
                alpha = targetAlpha
            }
            .shadow(targetElevation, RoundedCornerShape(8.dp)) // 드래그 시 그림자
            .background(White, RoundedCornerShape(8.dp)) // 배경색 (그림자 위해 필수)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
            .onGloballyPositioned { coordinates ->
                itemHeightPx = coordinates.size.height
            },
        verticalAlignment = Alignment.Top
    ) {
        // [좌측 영역] 텍스트 정보 + 드래그 핸들
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // ★ [핵심 4] 드래그 핸들 영역
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    isDragging = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    // dragOffset은 0에서 시작 (상대적 이동)
                                    dragOffset = 0f
                                },
                                onDragEnd = {
                                    isDragging = false
                                    dragOffset = 0f
                                },
                                onDragCancel = {
                                    isDragging = false
                                    dragOffset = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset += dragAmount.y

                                    // 아이템 높이 + 간격(12dp) = 전체 이동 단위
                                    val fullItemHeight = itemHeightPx + spacingPx

                                    // 임계값 (50% 이상 이동 시)
                                    val threshold = if (fullItemHeight > 0) fullItemHeight * 0.5f else 100f

                                    if (dragOffset > threshold) {
                                        // 아래로 이동
                                        if (currentIndex < totalCount - 1) {
                                            onMove(currentIndex, currentIndex + 1)
                                            // 이동 후 오프셋 보정 (시각적 위치 유지)
                                            dragOffset -= fullItemHeight
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    } else if (dragOffset < -threshold) {
                                        // 위로 이동
                                        if (currentIndex > 0) {
                                            onMove(currentIndex, currentIndex - 1)
                                            // 이동 후 오프셋 보정
                                            dragOffset += fullItemHeight
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.btn_toggle),
                        contentDescription = "순서 변경",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = SubBlack
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${item.price}원",
                style = MaterialTheme.typography.bodyMedium,
                color = SubGray
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // [우측 영역] 이미지 박스
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SubLightGray),
            contentAlignment = Alignment.Center
        ) {
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = "메뉴 사진",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_row_logo),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Preview
// -----------------------------------------------------------------------------
@Preview(name = "메뉴 탭 드래그 테스트", showBackground = true, heightDp = 1200)
@Composable
fun PreviewTabContentMenuWithData1() {
    SeatNowTheme {
        val dummyItems = listOf(
            StoreMenuItemData(1, "1. 나가사키 짬뽕탕", "22,000"),
            StoreMenuItemData(2, "2. 모듬 사시미", "35,000"),
            StoreMenuItemData(3, "3. 치킨 가라아게", "18,000")
        )
        val dummyCategories = listOf(
            StoreMenuCategory(1, "메인메뉴", dummyItems),
            StoreMenuCategory(2, "사이드메뉴", emptyList()),
            StoreMenuCategory(3, "주류", emptyList())
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(White),
            contentPadding = PaddingValues(24.dp)
        ) {
            items(dummyCategories) { category ->
                MenuCategoryItem(
                    category = category,
                    onAddMenuItemClick = {},
                    onCategoryEditClick = {},
                    onMoveItem = { from, to ->
                        println("Move: $from -> $to")
                    },
                    onMenuItemClick = {}
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}