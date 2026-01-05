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
import com.gmg.seatnow.domain.model.*
import com.gmg.seatnow.presentation.component.FloorFilterRow
import com.gmg.seatnow.presentation.component.SeatHeaderSection
import com.gmg.seatnow.presentation.component.SeatStatusSummary
import com.gmg.seatnow.presentation.component.TableStepperItem
import com.gmg.seatnow.presentation.component.TableViewItem
import com.gmg.seatnow.presentation.theme.*
import kotlinx.coroutines.flow.collectLatest

// 1. Stateful Screen (ViewModel 연결, 실제 앱에서 사용)
@Composable
fun SeatManagementScreen(
    viewModel: SeatManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // ★ [추가] ViewModel 이벤트 구독 (저장 성공/실패 토스트)
    LaunchedEffect(key1 = true) {
        viewModel.event.collectLatest { event ->
            when(event) {
                is SeatManagementEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    SeatManagementContent(
        uiState = uiState,
        onAction = viewModel::onAction
    )
}

// 2. Stateless Content (순수 UI 로직, Preview 가능)
@Composable
fun SeatManagementContent(
    uiState: SeatManagementViewModel.SeatManagementUiState,
    onAction: (SeatManagementAction) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White) // 배경색 명시
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. 헤더 (실시간 좌석 관리 + 토글 스위치)
            SeatHeaderSection(
                currentMode = uiState.displayMode,
                onModeChange = { mode ->
                    onAction(SeatManagementAction.ToggleDisplayMode(mode))
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 2. 층별 필터 (Chips)
            FloorFilterRow(
                categories = uiState.categories,
                selectedId = uiState.selectedCategoryId,
                onSelect = { id ->
                    onAction(SeatManagementAction.SelectCategory(id))
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. 통계 요약 (이용 좌석 수 / 전체 좌석 수)
            // 참고: ViewModel 로직 수정에 따라 emptySeats 대신 uiState.currentUsedSeats 등을 사용했을 수 있음
            // 여기서는 원본 코드 로직(emptySeats)을 유지하되, 필요시 수정하세요.
            SeatStatusSummary(
                mode = uiState.displayMode,
                totalSeats = uiState.totalSeatCapacity,
                usedSeats = uiState.currentUsedSeats
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 24.dp),
                thickness = 1.dp,
                color = SubLightGray
            )

            // 4. 테이블 리스트 (Stepper)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.weight(1f) // 남은 공간 차지
            ) {
                items(uiState.displayItems) { item ->
                    if (uiState.isEditMode) {
                        // 수정 모드 (사진 2): Stepper (+, - 버튼)
                        TableStepperItem(
                            item = item,
                            onIncrement = { onAction(SeatManagementAction.IncrementTableCount(item.id)) },
                            onDecrement = { onAction(SeatManagementAction.DecrementTableCount(item.id)) }
                        )
                    } else {
                        // 조회 모드 (사진 1): 단순 텍스트 ("2개")
                        TableViewItem(item = item)
                    }
                }
                // 하단 버튼에 가려지지 않게 여백 추가
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // 5. 하단 저장 버튼 (Floating처럼 하단 고정)
        Button(
            onClick = {
                if (uiState.isEditMode) {
                    // 수정 모드일 땐 '저장' 액션 -> 조회 모드로 변경됨
                    onAction(SeatManagementAction.OnSaveClick)
                } else {
                    // 조회 모드일 땐 '업데이트' 액션 -> 수정 모드로 변경됨
                    onAction(SeatManagementAction.OnUpdateClick)
                }
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
                text = if (uiState.isEditMode) "저장" else "업데이트",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

// 3. Preview (미리보기용 더미 데이터 구성)
@Preview(showBackground = true, heightDp = 800)
@Composable
fun SeatManagementScreenPreview() {
    // 더미 데이터 생성
    val mockItems = listOf(
        TableItem("1", "4인 테이블", 4, 5, 2), // 5개 중 2개 사용
        TableItem("2", "2인 테이블", 2, 3, 1)  // 3개 중 1개 사용
    )
    val mockCategories = listOf(
        FloorCategory("ALL", "전체"),
        FloorCategory("1F", "1층"),
        FloorCategory("2F", "2층")
    )

    val mockState = SeatManagementViewModel.SeatManagementUiState(
        categories = mockCategories,
        selectedCategoryId = "ALL",
        displayItems = mockItems,
        totalSeatCapacity = 26, // (4*5) + (2*3)
        currentUsedSeats = 10   // (4*2) + (2*1)
    )

    SeatNowTheme {
        SeatManagementContent(
            uiState = mockState,
            onAction = {}
        )
    }
}