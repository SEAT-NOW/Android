package com.gmg.seatnow.presentation.owner.store.seat

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.domain.model.FloorCategory
import com.gmg.seatnow.domain.model.TableItem
import com.gmg.seatnow.presentation.component.FloorFilterRow
import com.gmg.seatnow.presentation.component.SeatHeaderSection
import com.gmg.seatnow.presentation.component.SeatStatusSummary
import com.gmg.seatnow.presentation.component.TableStepperItem
import com.gmg.seatnow.presentation.component.TableViewItem
import com.gmg.seatnow.presentation.theme.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SeatManagementScreen(
    viewModel: SeatManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(true) {
        viewModel.loadData()
    }

    LaunchedEffect(key1 = true) {
        viewModel.event.collectLatest { event ->
            when(event) {
                is SeatManagementEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    SeatManagementContent(uiState = uiState, onAction = viewModel::onAction)
}

@Composable
fun SeatManagementContent(
    uiState: SeatManagementUiState,
    onAction: (SeatManagementAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. 헤더
            SeatHeaderSection(
                currentMode = uiState.displayState.displayMode,
                onModeChange = { mode -> onAction(SeatManagementAction.ToggleDisplayMode(mode)) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. 층별 필터
            FloorFilterRow(
                categories = uiState.seatData.categories,
                selectedId = uiState.displayState.selectedCategoryId,
                onSelect = { id -> onAction(SeatManagementAction.SelectCategory(id)) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
                thickness = 2.dp,
                color = SubLightGray
            )

            // 3. 통계 요약
            SeatStatusSummary(
                mode = uiState.displayState.displayMode,
                totalSeats = uiState.seatData.totalSeatCapacity,
                usedSeats = uiState.seatData.currentUsedSeats
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. 테이블 리스트 (섹션 포함)
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp) // 섹션 간 간격 조절
            ) {
                // Map 순회 (섹션 제목, 아이템 리스트)
                uiState.seatData.groupedDisplayItems.forEach { (sectionTitle, items) ->

                    // (1) 섹션 헤더 ("전체", "1층" 등)
                    item {
                        Text(
                            text = sectionTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SubGray,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    // (2) 해당 섹션의 아이템들
                    items(items) { item ->
                        // 수정 모드이면서 + "전체" 섹션이 아닌 경우에만 Stepper 표시
                        if (uiState.displayState.isEditMode) {
                            TableStepperItem(
                                item = item,
                                onIncrement = { onAction(SeatManagementAction.IncrementTableCount(item.id)) },
                                onDecrement = { onAction(SeatManagementAction.DecrementTableCount(item.id)) }
                            )
                        } else {
                            TableViewItem(item = item)
                        }
                        Spacer(modifier = Modifier.height(12.dp)) // 아이템 간 간격
                    }

                    // 섹션 구분선
                    item {
                        HorizontalDivider(color = SubPaleGray, thickness = 1.dp)
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // 5. 하단 버튼
        Button(
            onClick = {
                if (uiState.displayState.isEditMode) onAction(SeatManagementAction.OnSaveClick)
                else onAction(SeatManagementAction.OnUpdateClick)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PointRed,
                contentColor = White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (uiState.displayState.isEditMode) "저장" else "업데이트",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Preview(name = "1. 조회 모드 (전체 섹션 있음)", showBackground = true, heightDp = 800)
@Composable
fun SeatManagementScreenViewModePreview() {
    // 1. 더미 데이터 생성
    val item1F = TableItem(
        id = "1", floorId = "1F", label = "4인 테이블",
        capacityPerTable = 4, maxTableCount = 5, currentCount = 2
    )
    val item2F = TableItem(
        id = "2", floorId = "2F", label = "2인 테이블",
        capacityPerTable = 2, maxTableCount = 3, currentCount = 1
    )
    // 합쳐진 데이터 (조회용)
    val mergedItem = TableItem(
        id = "MERGED_4인", floorId = "ALL", label = "4인 테이블 (합계)",
        capacityPerTable = 4, maxTableCount = 10, currentCount = 4
    )

    val mockCategories = listOf(
        FloorCategory("ALL", "전체"),
        FloorCategory("1F", "1층"),
        FloorCategory("2F", "2층")
    )

    val viewModeMap = mapOf(
        "전체" to listOf(mergedItem),
        "1층" to listOf(item1F),
        "2층" to listOf(item2F)
    )

    val mockState = SeatManagementUiState(
        displayState = SeatManagementUiState.DisplayState(
            selectedCategoryId = "ALL",
            isEditMode = false
        ),
        seatData = SeatManagementUiState.SeatDataState(
            categories = mockCategories,
            groupedDisplayItems = viewModeMap,
            totalSeatCapacity = 26,
            currentUsedSeats = 10
        )
    )

    SeatNowTheme {
        SeatManagementContent(uiState = mockState, onAction = {})
    }
}

@Preview(name = "2. 업데이트 모드 (전체 섹션 제거됨)", showBackground = true, heightDp = 800)
@Composable
fun SeatManagementScreenEditModePreview() {
    // 1. 더미 데이터 (위와 동일)
    val item1F = TableItem(
        id = "1", floorId = "1F", label = "4인 테이블",
        capacityPerTable = 4, maxTableCount = 5, currentCount = 2
    )
    val item2F = TableItem(
        id = "2", floorId = "2F", label = "2인 테이블",
        capacityPerTable = 2, maxTableCount = 3, currentCount = 1
    )

    val mockCategories = listOf(
        FloorCategory("ALL", "전체"),
        FloorCategory("1F", "1층"),
        FloorCategory("2F", "2층")
    )

    val editModeMap = mapOf(
        "1층" to listOf(item1F),
        "2층" to listOf(item2F)
    )

    val mockState = SeatManagementUiState(
        displayState = SeatManagementUiState.DisplayState(
            selectedCategoryId = "ALL",
            isEditMode = true
        ),
        seatData = SeatManagementUiState.SeatDataState(
            categories = mockCategories,
            groupedDisplayItems = editModeMap,
            totalSeatCapacity = 26,
            currentUsedSeats = 10
        )
    )

    SeatNowTheme {
        SeatManagementContent(uiState = mockState, onAction = {})
    }
}