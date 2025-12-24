package com.gmg.seatnow.presentation.owner.signup.steps

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.component.*
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpViewModel.OwnerSignUpUiState
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpViewModel.SignUpAction
import com.gmg.seatnow.presentation.theme.*

@Composable
fun Step4OperatingScreen(
    uiState: OwnerSignUpUiState,
    onAction: (SignUpAction) -> Unit
) {
    // 정기 휴무일로 지정된 요일 계산 (운영 정보에서 비활성화용)
    val disabledOperatingDays = if (uiState.regularHolidayType == 1 && uiState.regularHolidayDay != null) {
        setOf(uiState.regularHolidayDay)
    } else {
        emptySet()
    }

    // ★ [수정] LazyColumn -> Column (부모 스크롤 사용)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp) // 하단 버튼 공간 확보
    ) {
        // --- 1. 정기 휴무일 섹션 ---
        Text(
            text = "정기 휴무일",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = SubBlack
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 1-1. 매주
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            RadioButton(
                selected = uiState.regularHolidayType == 1,
                onClick = { onAction(SignUpAction.SetRegularHolidayType(1)) },
                colors = RadioButtonDefaults.colors(selectedColor = PointRed, unselectedColor = SubGray),
                modifier = Modifier
                    .size(24.dp) // 1. 강제로 사이즈를 줄여서 Row 높이를 줄임
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("매주", style = MaterialTheme.typography.bodyMedium, color = SubBlack)
            Spacer(modifier = Modifier.width(12.dp))

            // 요일 선택 드롭다운
            SeatNowDropdownButton(
                text = uiState.regularHolidayDay?.let { listOf("일", "월", "화", "수", "목", "금", "토")[it] } ?: "일",
                onClick = { /* TODO: BottomSheet나 Dialog로 요일 선택 띄우기 */ }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("요일", style = MaterialTheme.typography.bodyMedium, color = SubBlack)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1-2. 매월
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            RadioButton(
                selected = uiState.regularHolidayType == 2,
                onClick = { onAction(SignUpAction.SetRegularHolidayType(2)) },
                colors = RadioButtonDefaults.colors(selectedColor = PointRed, unselectedColor = SubGray),
                modifier = Modifier
                    .size(24.dp) // 1. 강제로 사이즈를 줄여서 Row 높이를 줄임
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("매월", style = MaterialTheme.typography.bodyMedium, color = SubBlack)
            Spacer(modifier = Modifier.width(12.dp))

            // 주차 선택 드롭다운
            SeatNowDropdownButton(
                text = "${uiState.regularHolidayWeek ?: 1}주", // 예: 2·4주
                onClick = { /* TODO: 주차 선택 로직 */ },
                modifier = Modifier.widthIn(60.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            // 요일 선택 드롭다운
            SeatNowDropdownButton(
                text = "일", // 예시
                onClick = { /* TODO */ }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("요일", style = MaterialTheme.typography.bodyMedium, color = SubBlack)
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(thickness = 1.dp, color = SubLightGray.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. 임시 휴무 섹션 ---
        Text(
            text = "임시 휴무",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = SubBlack
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = uiState.regularHolidayType == 2,
                onClick = { onAction(SignUpAction.SetRegularHolidayType(2)) },
                colors = RadioButtonDefaults.colors(selectedColor = PointRed, unselectedColor = SubGray),
                modifier = Modifier
                    .size(24.dp) // 1. 강제로 사이즈를 줄여서 Row 높이를 줄임
            )
            Spacer(modifier = Modifier.width(8.dp))
            SeatNowDateBox(
                dateText = "2025/12/23", // 예시 데이터
                onClick = { /* DatePicker */ }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("~", style = MaterialTheme.typography.titleMedium, color = SubBlack)
            Spacer(modifier = Modifier.width(12.dp))
            SeatNowDateBox(
                dateText = "2025/12/23", // 예시 데이터
                onClick = { /* DatePicker */ }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(thickness = 1.dp, color = SubLightGray.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(24.dp))

        // --- 3. 운영 정보 섹션 ---
        Text(
            text = "운영 정보",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = SubBlack
        )
        Spacer(modifier = Modifier.height(16.dp))

        // ★ [수정] items() -> forEach 사용
        uiState.operatingSchedules.forEach { schedule ->
            Column(modifier = Modifier.fillMaxWidth()) {

                // 3-1. 요일 선택
                DayOfWeekSelector(
                    selectedDays = schedule.selectedDays,
                    disabledDays = disabledOperatingDays,
                    onDayClick = { dayIdx ->
                        onAction(SignUpAction.UpdateOperatingDays(schedule.id, dayIdx))
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 3-2. 시간 선택 (휠 피커)
                OperatingScheduleItem(
                    schedule = schedule,
                    // ★ 삭제 버튼 활성화 조건: 리스트 개수가 1개보다 많을 때만 true
                    isDeleteEnabled = uiState.operatingSchedules.size > 1,
                    onUpdateStart = { h, m ->
                        onAction(SignUpAction.UpdateOperatingTime(schedule.id, h, m / 5, schedule.endHour, schedule.endMin / 5))
                    },
                    onUpdateEnd = { h, m ->
                        onAction(SignUpAction.UpdateOperatingTime(schedule.id, schedule.startHour, schedule.startMin / 5, h, m / 5))
                    },
                    onDelete = {
                        onAction(SignUpAction.RemoveOperatingSchedule(schedule.id))
                    }
                )

            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 3-3. 추가 버튼
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            SeatNowRedPlusButton(
                onClick = { onAction(SignUpAction.AddOperatingSchedule) }
            )
        }
    }
}

@Preview(showBackground = true, name = "Step 4 Only", heightDp = 800)
@Composable
fun PreviewStep4OperatingScreen() {
    SeatNowTheme {
        Step4OperatingScreen (
            uiState = OwnerSignUpUiState(),
            onAction = {}
        )
    }
}