package com.gmg.seatnow.presentation.owner.store.mypage.storeManage.storeManageEdit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.StoreMenuCategory
import com.gmg.seatnow.presentation.component.SeatNowRedPlusButton
import com.gmg.seatnow.presentation.theme.*

@Composable
fun CategoryEditScreen(
    viewModel: StoreEditMainViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSaveEnabled = uiState.menuCategories.isNotEmpty()

    Scaffold(
        containerColor = White,
        // ★ Scaffold 기본 패딩 제거 (TopBar에서 직접 제어하기 위함)
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CategoryEditTopBar(onDismiss = onDismiss)
        },
        bottomBar = {
            CategoryEditBottomBar(
                isEnabled = isSaveEnabled,
                onSaveClick = { viewModel.onAction(StoreEditAction.SaveCategories) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "기본 제공되는 카테고리 외에 원하는 명칭으로 수정하거나 추가할 수 있습니다.",
                style = MaterialTheme.typography.bodySmall,
                color = SubGray,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(
                    items = uiState.menuCategories,
                    key = { _, category -> category.id }
                ) { index, category ->
                    CategoryEditItem(
                        modifier = Modifier.animateItem(),
                        category = category,
                        index = index,
                        totalCount = uiState.menuCategories.size,
                        onMove = { from, to ->
                            viewModel.onAction(StoreEditAction.MoveCategory(from, to))
                        },
                        onEditNameClick = { /* 이름 수정 로직 */ },
                        onDeleteClick = {
                            viewModel.onAction(StoreEditAction.DeleteCategory(category.id))
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        SeatNowRedPlusButton(
                            onClick = { viewModel.onAction(StoreEditAction.AddCategory) },
                            isEnabled = true
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun CategoryEditTopBar(onDismiss: () -> Unit) {
    // ★ [수정] Surface로 감싸서 배경색(White)이 상태 바 뒤까지 채워지도록 함
    Surface(
        color = White,
        shadowElevation = 0.dp // 필요시 그림자 추가
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding() // ★ 상태 바 높이만큼 패딩 추가
                .height(56.dp)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                tint = SubBlack,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onDismiss)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "메뉴 카테고리 편집",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SubBlack
            )
        }
    }
}

// ... (CategoryEditItem, CategoryEditBottomBar, Preview 등 기존 코드 유지)
// 코드가 길어 생략하나, 이전에 제공된 코드를 그대로 사용하시면 됩니다.
// (CategoryEditItem 드래그 로직은 이미 수정된 상태이므로 변경 불필요)
@Composable
fun CategoryEditItem(
    modifier: Modifier = Modifier,
    category: StoreMenuCategory,
    index: Int,
    totalCount: Int,
    onMove: (Int, Int) -> Unit,
    onEditNameClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // ★ [기존 코드 그대로 유지] (드래그 수정 로직 포함된 버전)
    val currentIndex by rememberUpdatedState(index)
    var itemHeightPx by remember { mutableIntStateOf(0) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val spacingPx = with(density) { 16.dp.toPx() }
    val targetScale by animateFloatAsState(targetValue = if (isDragging) 1.05f else 1f, label = "scale")
    val targetAlpha by animateFloatAsState(targetValue = if (isDragging) 0.7f else 1f, label = "alpha")
    val targetElevation = if (isDragging) 8.dp else 0.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                translationY = dragOffset
                scaleX = targetScale
                scaleY = targetScale
                alpha = targetAlpha
            }
            .shadow(targetElevation, RoundedCornerShape(8.dp))
            .background(White, RoundedCornerShape(8.dp))
            .padding(vertical = 12.dp, horizontal = 4.dp)
            .onGloballyPositioned { coordinates -> itemHeightPx = coordinates.size.height },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                isDragging = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                dragOffset = 0f
                            },
                            onDragEnd = { isDragging = false; dragOffset = 0f },
                            onDragCancel = { isDragging = false; dragOffset = 0f },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount.y
                                val fullItemHeight = itemHeightPx + spacingPx
                                val threshold = if (fullItemHeight > 0) fullItemHeight * 0.5f else 100f
                                if (dragOffset > threshold) {
                                    if (currentIndex < totalCount - 1) {
                                        onMove(currentIndex, currentIndex + 1)
                                        dragOffset -= fullItemHeight
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                } else if (dragOffset < -threshold) {
                                    if (currentIndex > 0) {
                                        onMove(currentIndex, currentIndex - 1)
                                        dragOffset += fullItemHeight
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                }
                            }
                        )
                    }
            ) {
                Icon(painter = painterResource(id = R.drawable.btn_toggle), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(15.dp))
            }
            Text(text = category.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = SubBlack)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(id = R.drawable.btn_menuedit), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(15.dp).clickable(onClick = onEditNameClick))
            Spacer(modifier = Modifier.width(12.dp))
            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = PointRed, modifier = Modifier.size(18.dp).clickable(onClick = onDeleteClick))
        }
    }
}

@Composable
fun CategoryEditBottomBar(isEnabled: Boolean, onSaveClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Button(
            onClick = onSaveClick,
            enabled = isEnabled,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PointRed, disabledContainerColor = SubLightGray, contentColor = White, disabledContentColor = White)
        ) {
            Text(text = "저장", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}