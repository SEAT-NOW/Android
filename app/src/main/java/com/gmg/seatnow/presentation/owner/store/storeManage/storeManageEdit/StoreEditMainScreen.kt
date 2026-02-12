package com.gmg.seatnow.presentation.owner.store.storeManage.storeManageEdit

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.domain.model.OperatingScheduleItem
import com.gmg.seatnow.presentation.theme.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun StoreEditMainScreen(
    viewModel: StoreEditMainViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 1. 카테고리 순서 편집 모드일 때 뒤로가기 -> 모드 종료
    BackHandler(enabled = uiState.isCategoryEditMode) {
        viewModel.onAction(StoreEditAction.SetCategoryEditMode(false))
    }

    // 2. 메뉴 추가 모드일 때 뒤로가기 -> 모드 종료
    BackHandler(enabled = uiState.addingMenuCategoryId != null) {
        viewModel.onAction(StoreEditAction.DismissAddMenu)
    }

    LaunchedEffect(true) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is StoreEditMainEvent.NavigateBack -> onNavigateBack()
                is StoreEditMainEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 화면 상태에 따른 분기 처리
    when {
        // [Case 1] 카테고리 편집 모드 (순서 변경 등)
        uiState.isCategoryEditMode -> {
            CategoryEditScreen(
                viewModel = viewModel,
                onDismiss = { viewModel.onAction(StoreEditAction.SetCategoryEditMode(false)) }
            )
        }

        // [Case 2] 메뉴 상세 추가 모드 (플러스 버튼 클릭 시)
        uiState.addingMenuCategoryId != null -> {
            MenuEditScreen(
                initialCategoryId = uiState.addingMenuCategoryId!!,
                categoryList = uiState.menuCategories,
                onBackClick = { viewModel.onAction(StoreEditAction.DismissAddMenu) },
                onDeleteClick = { viewModel.onAction(StoreEditAction.DismissAddMenu) }, // 추가 취소
                onSaveClick = { name, price, catId, uri ->
                    viewModel.onAction(
                        StoreEditAction.ConfirmAddMenu(
                            categoryId = catId,
                            name = name,
                            price = price,
                            imageUri = uri?.toString()
                        )
                    )
                }
            )
        }

        uiState.editingMenuItem != null -> {
            val (categoryId, item) = uiState.editingMenuItem!!

            MenuEditScreen(
                initialCategoryId = categoryId,
                initialName = item.name,           // ★ 기존 이름 주입
                initialPrice = item.price,         // ★ 기존 가격 주입
                initialImageUri = item.imageUrl,   // ★ 기존 이미지 주입

                categoryList = uiState.menuCategories,

                onBackClick = { viewModel.onAction(StoreEditAction.DismissEditMenu) },

                // ★ 삭제 버튼 -> 실제 삭제 액션 호출
                onDeleteClick = {
                    viewModel.onAction(StoreEditAction.DeleteMenuItem(categoryId, item.id))
                },

                // ★ 저장 버튼 -> 수정(Update) 액션 호출
                onSaveClick = { name, price, newCatId, uri ->
                    val updatedItem = item.copy(
                        name = name,
                        price = price,
                        imageUrl = uri?.toString()
                    )
                    viewModel.onAction(
                        StoreEditAction.UpdateMenuItem(
                            originalCategoryId = categoryId,
                            newCategoryId = newCatId,
                            updatedItem = updatedItem
                        )
                    )
                }
            )
        }

        // [Case 3] 기본 메인 편집 화면
        else -> {
            Scaffold(
                containerColor = White,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    StoreEditMainTopBar(
                        isSaveEnabled = uiState.isSaveButtonEnabled,
                        onBackClick = onNavigateBack,
                        onSaveClick = { viewModel.onSaveClick() }
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // 2. Custom Tab Bar
                    Box(modifier = Modifier.zIndex(1f)) {
                        StoreEditMainTabBar(
                            selectedTabIndex = uiState.selectedTabIndex,
                            onTabSelected = { viewModel.onTabSelected(it) }
                        )
                    }

                    // 3. Tab Contents
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(White)
                    ) {
                        when (uiState.selectedTabIndex) {
                            0 -> TabContentOperationInfo(
                                uiState = uiState,
                                onAction = viewModel::onAction
                            )
                            // ViewModel을 전달하여 내부에서 OpenAddMenu 액션 호출 가능
                            1 -> TabContentMenu(viewModel = viewModel)
                            2 -> TabContentStorePhotos(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoreEditMainTopBar(
    isSaveEnabled: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Surface(
        shadowElevation = 2.dp,
        color = White,
        modifier = Modifier.zIndex(2f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onBackClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "뒤로가기",
                    tint = SubBlack,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "가게 정보 편집",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SubBlack
                )
            }

            Button(
                onClick = onSaveClick,
                enabled = isSaveEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PointRed,
                    contentColor = White,
                    disabledContainerColor = SubLightGray,
                    disabledContentColor = SubGray
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .height(32.dp)
                    .width(50.dp)
            ) {
                Text(
                    text = "저장",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun StoreEditMainTabBar(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("운영정보", "메뉴", "가게사진")

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp,
        color = White,
        shape = RectangleShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTabIndex == index

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) PointRed else SubGray
                    )

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(PointRed)
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .height(12.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            PointRed.copy(alpha = 0f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(SubLightGray)
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "가게 정보 수정 메인 (전체 화면)", showBackground = true, heightDp = 900)
@Composable
fun PreviewStoreEditMainScreen() {
    SeatNowTheme {
        // 1. Preview를 위한 로컬 상태 관리
        var selectedTabIndex by remember { mutableIntStateOf(0) }

        // 2. 더미 데이터 생성
        val dummyUiState = StoreEditMainViewModel.StoreEditUiState(
            selectedTabIndex = selectedTabIndex,
            isSaveButtonEnabled = true,
            regularHolidayType = 1,
            weeklyHolidayDays = setOf(1),
            operatingSchedules = listOf(
                OperatingScheduleItem(
                    id = 0,
                    selectedDays = setOf(1, 2, 3, 4, 5),
                    startHour = 10, startMin = 0,
                    endHour = 22, endMin = 0
                )
            )
        )

        // 3. UI 레이아웃
        Scaffold(
            containerColor = White,
            topBar = {
                StoreEditMainTopBar(
                    isSaveEnabled = dummyUiState.isSaveButtonEnabled,
                    onBackClick = {},
                    onSaveClick = {}
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Box(modifier = Modifier.zIndex(1f)) {
                    StoreEditMainTabBar(
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = { selectedTabIndex = it }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(White)
                ) {
                    when (selectedTabIndex) {
                        0 -> TabContentOperationInfo(
                            uiState = dummyUiState,
                            onAction = {}
                        )
                        1 -> TabContentMenu()
                        2 -> TabContentStorePhotos()
                    }
                }
            }
        }
    }
}