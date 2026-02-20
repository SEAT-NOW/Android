package com.gmg.seatnow.presentation.owner.store.storeManage.storeManageEdit

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.StoreMenuCategory
import com.gmg.seatnow.presentation.component.SeatNowRedPlusButton
import com.gmg.seatnow.presentation.extension.bottomShadow
import com.gmg.seatnow.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditScreen(
    viewModel: StoreEditMainViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSaveEnabled = uiState.menuCategories.isNotEmpty()

    // 드래그 중인 아이템 식별
    var draggingItemId by remember { mutableStateOf<Long?>(null) }

    // 1. 카테고리 이름 변경 다이얼로그
    if (uiState.editingCategory != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onAction(StoreEditAction.DismissRenameDialog) },
            containerColor = White,
            dragHandle = null
        ) {
            CategoryInputBottomSheet(
                title = "카테고리명 변경",
                initialName = uiState.editingCategory!!.name,
                onDismiss = { viewModel.onAction(StoreEditAction.DismissRenameDialog) },
                onConfirm = { newName ->
                    viewModel.onAction(StoreEditAction.UpdateCategoryName(uiState.editingCategory!!.id, newName))
                }
            )
        }
    }

    // ★ 2. 카테고리 추가 다이얼로그 (동일한 UI 재사용)
    if (uiState.isAddingCategory) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onAction(StoreEditAction.DismissAddCategoryDialog) },
            containerColor = White,
            dragHandle = null
        ) {
            CategoryInputBottomSheet(
                title = "카테고리 추가",
                initialName = "", // 빈 값으로 시작
                onDismiss = { viewModel.onAction(StoreEditAction.DismissAddCategoryDialog) },
                onConfirm = { newName ->
                    viewModel.onAction(StoreEditAction.ConfirmAddCategory(newName))
                }
            )
        }
    }

    Scaffold(
        containerColor = White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { CategoryEditTopBar(onDismiss = onDismiss) },
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
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(
                    items = uiState.menuCategories,
                    key = { _, category -> category.id }
                ) { index, category ->

                    val itemModifier = if (draggingItemId == category.id) {
                        Modifier.zIndex(1f)
                    } else {
                        Modifier.animateItem()
                    }

                    CategoryEditItem(
                        modifier = itemModifier,
                        category = category,
                        index = index,
                        totalCount = uiState.menuCategories.size,
                        onDragStart = { draggingItemId = category.id },
                        onDragEnd = { draggingItemId = null },
                        onMove = { from, to ->
                            viewModel.onAction(StoreEditAction.MoveCategory(from, to))
                        },
                        onEditNameClick = { viewModel.onAction(StoreEditAction.OpenRenameDialog(category)) },
                        onDeleteClick = { viewModel.onAction(StoreEditAction.DeleteCategory(category.id)) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        SeatNowRedPlusButton(
                            // ★ [연결] 플러스 버튼 클릭 시 추가 다이얼로그 오픈
                            onClick = { viewModel.onAction(StoreEditAction.OpenAddCategoryDialog) },
                            isEnabled = true
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// ★ [수정] 공용 카테고리 입력 바텀시트 (변경/추가 공용)
// ---------------------------------------------------------------------------------------------
@Composable
fun CategoryInputBottomSheet(
    title: String,
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialName) }
    val isButtonEnabled = text.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White) // ★ 배경색 White 명시
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
    ) {
        // 1. 헤더 (타이틀 + X버튼) - 패딩 8dp로 축소
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = SubBlack,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                tint = SubLightGray,
                modifier = Modifier
                    .size(18.dp) // ★ X 버튼 크기 18dp 축소
                    .align(Alignment.CenterEnd)
                    .clickable(onClick = onDismiss)
            )
        }

        // 2. Divider
        HorizontalDivider(thickness = 1.dp, color = SubLightGray.copy(alpha = 0.5f))

        Spacer(modifier = Modifier.height(24.dp))

        // 3. 텍스트 필드 (입력값 bodyMedium 적용됨)
        SeatNowClearableTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = "카테고리명을 입력해주세요",
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 4. 확인 버튼
        Button(
            onClick = { onConfirm(text) },
            enabled = isButtonEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PointRed,
                disabledContainerColor = SubLightGray,
                contentColor = White,
                disabledContentColor = White
            )
        ) {
            Text(
                text = "확인",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// ★ [유지] X버튼(삭제)이 포함된 TextField 컴포넌트
// ---------------------------------------------------------------------------------------------
@Composable
fun SeatNowClearableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = if (isFocused) SubBlack else SubLightGray

    Column(modifier = modifier.fillMaxWidth()) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            interactionSource = interactionSource,
            // ★ [확인] 입력 텍스트 스타일: bodyMedium
            textStyle = Body1_Medium_14.copy(color = SubBlack),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .bottomShadow(offsetY = 2.dp, shadowBlurRadius = 4.dp, alpha = 0.15f, cornersRadius = 12.dp)
                .background(color = White, shape = RoundedCornerShape(12.dp))
                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp)),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && !isFocused) {
                            Text(text = placeholder, color = SubLightGray, style = MaterialTheme.typography.bodyMedium)
                        }
                        innerTextField()
                    }

                    if (value.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "삭제",
                            tint = White,
                            modifier = Modifier
                                .size(20.dp)
                                .background(SubLightGray, CircleShape)
                                .clip(CircleShape)
                                .clickable { onValueChange("") }
                                .padding(3.dp)
                        )
                    }
                }
            }
        )
    }
}

// ... (CategoryEditTopBar, CategoryEditItem, CategoryEditBottomBar 기존 코드 유지) ...
@Composable
fun CategoryEditTopBar(onDismiss: () -> Unit) {
    Surface(color = White, shadowElevation = 0.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().height(56.dp).padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Close, "닫기", tint = SubBlack, modifier = Modifier.size(24.dp).clickable(onClick = onDismiss))
            Spacer(modifier = Modifier.width(12.dp))
            Text("메뉴 카테고리 편집", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SubBlack)
        }
    }
}

@Composable
fun CategoryEditItem(
    modifier: Modifier = Modifier,
    category: StoreMenuCategory,
    index: Int,
    totalCount: Int,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onMove: (Int, Int) -> Unit,
    onEditNameClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val currentIndex by rememberUpdatedState(index)
    var itemHeightPx by remember { mutableIntStateOf(0) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val spacingPx = with(density) { 16.dp.toPx() } // 아이템 간격

    // 애니메이션
    val targetScale by animateFloatAsState(targetValue = if (isDragging) 1.05f else 1f, label = "scale")
    val targetAlpha by animateFloatAsState(targetValue = if (isDragging) 0.7f else 1f, label = "alpha")
    val targetElevation = if (isDragging) 8.dp else 0.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
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
            // [드래그 핸들]
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                isDragging = true
                                onDragStart()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                dragOffset = 0f
                            },
                            onDragEnd = {
                                isDragging = false
                                onDragEnd()
                                dragOffset = 0f
                            },
                            onDragCancel = {
                                isDragging = false
                                onDragEnd()
                                dragOffset = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount.y

                                val fullItemHeight = itemHeightPx + spacingPx
                                val threshold = if (fullItemHeight > 0) fullItemHeight * 0.5f else 100f

                                // [핵심 로직] 임계값을 넘으면 순서 변경 요청
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
                Icon(
                    painter = painterResource(id = R.drawable.btn_toggle),
                    contentDescription = "순서 변경",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(15.dp)
                )
            }
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = SubBlack
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.btn_menuedit),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(15.dp).clickable(onClick = onEditNameClick)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = PointRed,
                modifier = Modifier.size(18.dp).clickable(onClick = onDeleteClick)
            )
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
