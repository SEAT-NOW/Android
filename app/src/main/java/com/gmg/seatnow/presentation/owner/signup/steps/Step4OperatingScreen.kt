package com.gmg.seatnow.presentation.owner.signup.steps

import androidx.compose.foundation.layout.*
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step4OperatingScreen(
    uiState: OwnerSignUpUiState,
    onAction: (SignUpAction) -> Unit
) {
    val disabledOperatingDays = if (uiState.regularHolidayType == 1) uiState.weeklyHolidayDays else emptySet()
    val daysText = listOf("일", "월", "화", "수", "목", "금", "토")

    // Helper: 요일 Set -> "월 · 화" 변환
    fun formatDays(indices: Set<Int>): String =
        if(indices.isEmpty()) "요일 선택" else indices.sorted().joinToString(" · ") { daysText[it] }

    // Helper: 주차 Set -> "2 · 4 주" 변환
    fun formatWeeks(indices: Set<Int>): String =
        if (indices.isEmpty()) "주 선택" else indices.sorted().joinToString(" · ") + " 주"

    fun millisToDate(millis: Long?): String =
        if (millis != null) SimpleDateFormat("yyyy/MM/dd", Locale.KOREA).format(Date(millis)) else ""

    // ★ [수정] LazyColumn -> Column (부모 스크롤 사용)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 30.dp) // 하단 버튼 공간 확보
    ) {
        Text(
            text = "정기 휴무일",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = SubBlack
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 1-1. 매주 (Radio Button Logic)
        val isWeeklySelected = uiState.regularHolidayType == 1
        val weeklyTextColor = if (isWeeklySelected) PointRed else SubGray
        val weeklyBorderColor = if (isWeeklySelected) PointRed else SubLightGray

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            SeatNowCheckRadioButton(
                selected = isWeeklySelected,
                onClick = { onAction(SignUpAction.ToggleRegularHolidayType(1))}
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("매주", style = MaterialTheme.typography.bodyMedium, color = weeklyTextColor)
            Spacer(modifier = Modifier.width(12.dp))

            // 요일 선택 드롭다운
            val displayWeeklyText = formatDays(uiState.weeklyHolidayDays)
            SeatNowDropdownButton(
                text = displayWeeklyText,
                onClick = { onAction(SignUpAction.SetWeeklyDialogVisible(true)) },
                enabled = isWeeklySelected,
                modifier = Modifier.widthIn(min = 40.dp),
                borderColor = weeklyBorderColor,
                textColor = weeklyTextColor
            )

            Spacer(modifier = Modifier.width(8.dp))
            Text("요일", style = MaterialTheme.typography.bodyMedium, color = weeklyTextColor)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1-2. 매월
        val isMonthlySelected = uiState.regularHolidayType == 2
        val monthlyTextColor = if (isMonthlySelected) PointRed else SubGray
        val monthlyBorderColor = if (isMonthlySelected) PointRed else SubLightGray

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            SeatNowCheckRadioButton(
                selected = isMonthlySelected,
                onClick = { onAction(SignUpAction.ToggleRegularHolidayType(2)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("매월", style = MaterialTheme.typography.bodyMedium, color = monthlyTextColor)
            Spacer(modifier = Modifier.width(12.dp))

            val displayMonthlyWeekText = formatWeeks(uiState.monthlyHolidayWeeks)
            SeatNowDropdownButton(
                text = displayMonthlyWeekText,
                onClick = { onAction(SignUpAction.SetMonthlyWeekDialogVisible(true)) },
                enabled = isMonthlySelected,
                modifier = Modifier.widthIn(min = 60.dp),
                borderColor = monthlyBorderColor,
                textColor = monthlyTextColor
            )
            Spacer(modifier = Modifier.width(8.dp))

            val displayMonthlyDayText = formatDays(uiState.monthlyHolidayDays)
            SeatNowDropdownButton(
                text = displayMonthlyDayText,
                onClick = { onAction(SignUpAction.SetMonthlyDayDialogVisible(true)) },
                enabled = isMonthlySelected,
                modifier = Modifier.widthIn(min = 40.dp),
                borderColor = monthlyBorderColor,
                textColor = monthlyTextColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("요일", style = MaterialTheme.typography.bodyMedium, color = monthlyTextColor)
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
            SeatNowCheckRadioButton(
                selected = uiState.isTempHolidayEnabled,
                onClick = { onAction(SignUpAction.ToggleTempHoliday) },
            )
            Spacer(modifier = Modifier.width(8.dp))

            val tempHolidayBorderColor = if(uiState.isTempHolidayEnabled) PointRed else SubLightGray
            val tempHolidayTextColor = if(uiState.isTempHolidayEnabled) PointRed else SubGray

            SeatNowDateBox(
                dateText = if (uiState.tempHolidayStart.isNotEmpty()) uiState.tempHolidayStart else "YYYY/MM/DD",
                onClick = { if (uiState.isTempHolidayEnabled) onAction(SignUpAction.SetTempHolidayDatePickerVisible(true)) },
                enabled = uiState.isTempHolidayEnabled,
                modifier = Modifier.widthIn(max = 120.dp),
                borderColor = tempHolidayBorderColor,
                textColor = tempHolidayTextColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("~", style = MaterialTheme.typography.titleMedium, color = SubBlack)
            Spacer(modifier = Modifier.width(12.dp))
            // 종료일
            SeatNowDateBox(
                dateText = if (uiState.tempHolidayEnd.isNotEmpty()) uiState.tempHolidayEnd else "YYYY/MM/DD",
                onClick = { if (uiState.isTempHolidayEnabled) onAction(SignUpAction.SetTempHolidayDatePickerVisible(true)) },
                enabled = uiState.isTempHolidayEnabled,
                modifier = Modifier.widthIn(max = 120.dp),
                borderColor = tempHolidayBorderColor,
                textColor = tempHolidayTextColor
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

        uiState.operatingSchedules.forEach { schedule ->
            Column(modifier = Modifier.fillMaxWidth()) {
                DayOfWeekSelector(
                    selectedDays = schedule.selectedDays,
                    disabledDays = disabledOperatingDays,
                    onDayClick = { dayIdx -> onAction(SignUpAction.UpdateOperatingDays(schedule.id, dayIdx)) }
                )
                if (schedule.selectedDays.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    OperatingScheduleItem(
                        schedule = schedule,
                        isDeleteEnabled = uiState.operatingSchedules.size > 1,
                        onUpdateStart = { h, m -> onAction(SignUpAction.UpdateOperatingTime(schedule.id, h, m, schedule.endHour, schedule.endMin)) },
                        onUpdateEnd = { h, m -> onAction(SignUpAction.UpdateOperatingTime(schedule.id, schedule.startHour, schedule.startMin, h, m)) },
                        onDelete = { onAction(SignUpAction.RemoveOperatingSchedule(schedule.id)) }
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (schedule.selectedDays.isEmpty()) {
                HorizontalDivider(thickness = 1.dp, color = SubPaleGray)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            SeatNowRedPlusButton(onClick = { onAction(SignUpAction.AddOperatingSchedule) })
        }

        if (uiState.showWeeklyDayDialog) {
            WeeklyHolidayDialog(
                selectedDays = uiState.weeklyHolidayDays,
                onDismiss = { onAction(SignUpAction.SetWeeklyDialogVisible(false)) },
                onConfirm = { days -> onAction(SignUpAction.UpdateWeeklyHolidays(days)) }
            )
        }

        if (uiState.showMonthlyWeekDialog) {
            MonthlyWeekDialog(
                selectedWeeks = uiState.monthlyHolidayWeeks,
                onDismiss = { onAction(SignUpAction.SetMonthlyWeekDialogVisible(false)) },
                onConfirm = { weeks -> onAction(SignUpAction.UpdateMonthlyWeeks(weeks)) }
            )
        }
        if (uiState.showMonthlyDayDialog) {
            WeeklyHolidayDialog(
                selectedDays = uiState.monthlyHolidayDays,
                onDismiss = { onAction(SignUpAction.SetMonthlyDayDialogVisible(false)) },
                onConfirm = { days -> onAction(SignUpAction.UpdateMonthlyDays(days)) } // UpdateMonthlyDays 호출
            )
        }


        // ★ [Fix] DateRangePicker Logic
        if (uiState.showTempHolidayDatePicker) {
            val datePickerState = rememberDateRangePickerState()
            DatePickerDialog(
                onDismissRequest = { onAction(SignUpAction.SetTempHolidayDatePickerVisible(false)) },
                confirmButton = {
                    TextButton(onClick = {
                        val startStr = millisToDate(datePickerState.selectedStartDateMillis)
                        val endStr = millisToDate(datePickerState.selectedEndDateMillis)
                        // 시작일만 선택하고 종료일 선택 안했을 경우 방어 로직 (시작일 = 종료일)
                        val finalEndStr = if (endStr.isEmpty()) startStr else endStr

                        if (startStr.isNotEmpty()) {
                            onAction(SignUpAction.UpdateTempHolidayRange(startStr, finalEndStr))
                        }
                    }) { Text("저장") }
                },
                colors = DatePickerDefaults.colors(containerColor = White) // 다이얼로그 배경 흰색
            ) {
                DateRangePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(containerColor = White)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Step 4 (매주 선택)", heightDp = 800)
@Composable
fun PreviewStep4WeeklySelected() {
    SeatNowTheme {
        Step4OperatingScreen(
            // regularHolidayType = 1로 설정하여 '매주'가 선택된 상태를 보여줌
            uiState = OwnerSignUpUiState(
                regularHolidayType = 1,
//                weeklyHolidayDays = setOf(0) // 예: 일요일 선택된 상태
            ),
            onAction = {}
        )
    }
}

@Preview(showBackground = true, name = "Step 4 (매월 선택)", heightDp = 800)
@Composable
fun PreviewStep4MonthlySelected() {
    SeatNowTheme {
        Step4OperatingScreen(
            // regularHolidayType = 2로 설정하여 '매월'이 선택된 상태를 보여줌
            uiState = OwnerSignUpUiState(
                regularHolidayType = 2,
//                monthlyHolidayWeeks = setOf(2, 4), // 예: 2, 4주 선택된 상태
//                monthlyHolidayDays = setOf(0)      // 예: 일요일 선택된 상태
            ),
            onAction = {}
        )
    }
}