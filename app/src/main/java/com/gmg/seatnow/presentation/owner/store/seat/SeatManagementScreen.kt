package com.gmg.seatnow.presentation.owner.store.seat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.component.FloorFilterRow
import com.gmg.seatnow.presentation.component.SeatHeaderSection
import com.gmg.seatnow.presentation.component.SeatStatusSummary
import com.gmg.seatnow.presentation.component.TableStepperItem
import com.gmg.seatnow.presentation.theme.*

@Composable
fun SeatManagementScreen(
    viewModel: SeatManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. 헤더 (실시간 좌석 관리 + 토글 스위치)
            // SeatNowComponents.kt에서 가져옴
            SeatHeaderSection()

            Spacer(modifier = Modifier.height(20.dp))

            // 2. 층별 필터 (Chips)
            // SeatNowComponents.kt에서 가져옴
            FloorFilterRow(
                categories = uiState.categories,
                selectedId = uiState.selectedCategoryId,
                onSelect = viewModel::onCategorySelected
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. 통계 요약 (빈 좌석 수 / 전체 좌석 수)
            // SeatNowComponents.kt에서 가져옴
            SeatStatusSummary(
                emptySeats = uiState.totalSeatCapacity - uiState.currentUsedSeats,
                totalSeats = uiState.totalSeatCapacity
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 24.dp),
                thickness = 1.dp,
                color = SubLightGray
            )

            // 4. 테이블 리스트 (Stepper)
            // SeatNowComponents.kt에서 가져옴
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.weight(1f) // 남은 공간 차지
            ) {
                items(uiState.displayItems) { item ->
                    TableStepperItem(
                        item = item,
                        onIncrement = { viewModel.onIncrement(item.id) },
                        onDecrement = { viewModel.onDecrement(item.id) }
                    )
                }
                // 하단 버튼에 가려지지 않게 여백 추가
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        // 5. 하단 저장 버튼 (Floating처럼 하단 고정)
        Button(
            onClick = viewModel::onSave,
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
                text = "저장",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}