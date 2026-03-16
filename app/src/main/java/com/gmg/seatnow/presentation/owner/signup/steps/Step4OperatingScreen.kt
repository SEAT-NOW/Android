package com.gmg.seatnow.presentation.owner.signup.steps

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.component.*
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpUiState
import com.gmg.seatnow.presentation.owner.signup.OperationState // ★ Preview용 추가
import com.gmg.seatnow.presentation.owner.signup.SignUpAction
import com.gmg.seatnow.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step4OperatingScreen(
    uiState: OwnerSignUpUiState,
    onAction: (SignUpAction) -> Unit
) {
    // ★ operation 바구니 참조
    val disabledOperatingDays =
        if (uiState.operation.regularHolidayType == 1) uiState.operation.weeklyHolidayDays else emptySet()

    val scheduledDays = uiState.operation.operatingSchedules.flatMap { it.selectedDays }.toSet()

    val isWeekFull = (disabledOperatingDays + scheduledDays).size >= 7

    val daysText = listOf("일", "월", "화", "수", "목", "금", "토")

    // Helper: 요일 Set -> "월 · 화" 변환
    fun formatDays(indices: Set<Int>): String =
        if (indices.isEmpty()) "요일 선택" else indices.sorted().joinToString(" · ") { daysText[it] }

    // Helper: 주차 Set -> "2 · 4 주" 변환
    fun formatWeeks(indices: Set<Int>): String =
        if (indices.isEmpty()) "주 선택" else indices.sorted().joinToString(" · ") + " 주"

    fun millisToDate(millis: Long?): String =
        if (millis != null) SimpleDateFormat(
            "yyyy/MM/dd",
            Locale.KOREA
        ).format(Date(millis)) else ""

    var expandedScheduleId by remember { mutableStateOf<Long?>(null) }
    var expandedTimeTarget by remember { mutableStateOf(TimeTarget.None) }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isSmallScreen = maxWidth < 380.dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp) // 하단 버튼 공간 확보
        ) {
            Text(
                text = "정기 휴무일",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = SubBlack
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 1-1. 매주 (Radio Button Logic)
            val isWeeklySelected = uiState.operation.regularHolidayType == 1
            val weeklyTextColor = if (isWeeklySelected) PointRed else SubGray
            val weeklyBorderColor = if (isWeeklySelected) PointRed else SubLightGray

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                SeatNowCheckRadioButton(
                    selected = isWeeklySelected,
                    onClick = { onAction(SignUpAction.ToggleRegularHolidayType(1)) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("매주", style = MaterialTheme.typography.bodyMedium, color = weeklyTextColor)
                Spacer(modifier = Modifier.width(12.dp))

                // 요일 선택 드롭다운
                val displayWeeklyText = formatDays(uiState.operation.weeklyHolidayDays)
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
            val isMonthlySelected = uiState.operation.regularHolidayType == 2
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

                val displayMonthlyWeekText = formatWeeks(uiState.operation.monthlyHolidayWeeks)
                SeatNowDropdownButton(
                    text = displayMonthlyWeekText,
                    onClick = { onAction(SignUpAction.SetMonthlyWeekDialogVisible(true)) },
                    enabled = isMonthlySelected,
                    modifier = Modifier.widthIn(min = 60.dp),
                    borderColor = monthlyBorderColor,
                    textColor = monthlyTextColor
                )
                Spacer(modifier = Modifier.width(8.dp))

                val displayMonthlyDayText = formatDays(uiState.operation.monthlyHolidayDays)
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
                    selected = uiState.operation.isTempHolidayEnabled,
                    onClick = { onAction(SignUpAction.ToggleTempHoliday) },
                )
                Spacer(modifier = Modifier.width(8.dp))

                val tempHolidayBorderColor =
                    if (uiState.operation.isTempHolidayEnabled) PointRed else SubLightGray
                val tempHolidayTextColor = if (uiState.operation.isTempHolidayEnabled) PointRed else SubGray

                SeatNowDateBox(
                    dateText = if (uiState.operation.tempHolidayStart.isNotEmpty()) uiState.operation.tempHolidayStart else "YYYY/MM/DD",
                    onClick = {
                        if (uiState.operation.isTempHolidayEnabled) onAction(
                            SignUpAction.SetTempHolidayDatePickerVisible(
                                true
                            )
                        )
                    },
                    enabled = uiState.operation.isTempHolidayEnabled,
                    modifier = Modifier.widthIn(max = 120.dp),
                    borderColor = tempHolidayBorderColor,
                    textColor = tempHolidayTextColor
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("~", style = MaterialTheme.typography.titleMedium, color = SubBlack)
                Spacer(modifier = Modifier.width(12.dp))
                // 종료일
                SeatNowDateBox(
                    dateText = if (uiState.operation.tempHolidayEnd.isNotEmpty()) uiState.operation.tempHolidayEnd else "YYYY/MM/DD",
                    onClick = {
                        if (uiState.operation.isTempHolidayEnabled) onAction(
                            SignUpAction.SetTempHolidayDatePickerVisible(
                                true
                            )
                        )
                    },
                    enabled = uiState.operation.isTempHolidayEnabled,
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

            uiState.operation.operatingSchedules.forEach { schedule ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    DayOfWeekSelector(
                        selectedDays = schedule.selectedDays,
                        disabledDays = disabledOperatingDays,
                        buttonSize = if (isSmallScreen) 34.dp else 40.dp,
                        onDayClick = { dayIdx ->
                            onAction(
                                SignUpAction.UpdateOperatingDays(
                                    schedule.id,
                                    dayIdx
                                )
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    val isMyStartOpen =
                        (expandedScheduleId == schedule.id && expandedTimeTarget == TimeTarget.Start)
                    val isMyEndOpen =
                        (expandedScheduleId == schedule.id && expandedTimeTarget == TimeTarget.End)

                    OperatingScheduleItemRow(
                        schedule = schedule,
                        isDeleteEnabled = uiState.operation.operatingSchedules.size > 1,
                        expandedTarget = if (isMyStartOpen) TimeTarget.Start else if (isMyEndOpen) TimeTarget.End else TimeTarget.None,
                        isSmallScreen = isSmallScreen,

                        onToggleStart = {
                            if (isMyStartOpen) {
                                expandedScheduleId = null
                                expandedTimeTarget = TimeTarget.None
                            } else {
                                expandedScheduleId = schedule.id
                                expandedTimeTarget = TimeTarget.Start
                            }
                        },
                        onToggleEnd = {
                            if (isMyEndOpen) {
                                expandedScheduleId = null
                                expandedTimeTarget = TimeTarget.None
                            } else {
                                expandedScheduleId = schedule.id
                                expandedTimeTarget = TimeTarget.End
                            }
                        },
                        onUpdateStart = { h, m ->
                            onAction(
                                SignUpAction.UpdateOperatingTime(
                                    schedule.id,
                                    h,
                                    m,
                                    schedule.endHour,
                                    schedule.endMin
                                )
                            )
                        },
                        onUpdateEnd = { h, m ->
                            onAction(
                                SignUpAction.UpdateOperatingTime(
                                    schedule.id,
                                    schedule.startHour,
                                    schedule.startMin,
                                    h,
                                    m
                                )
                            )
                        },
                        onDelete = { onAction(SignUpAction.RemoveOperatingSchedule(schedule.id)) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }

                if (schedule != uiState.operation.operatingSchedules.last()) {
                    HorizontalDivider(thickness = 1.dp, color = SubPaleGray)
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SeatNowRedPlusButton(
                    onClick = { onAction(SignUpAction.AddOperatingSchedule) },
                    isEnabled = !isWeekFull // 꽉 찼으면 false (회색, 클릭 불가)
                )
            }

            if (uiState.operation.showWeeklyDayDialog) {
                WeeklyHolidayDialog(
                    selectedDays = uiState.operation.weeklyHolidayDays,
                    onDismiss = { onAction(SignUpAction.SetWeeklyDialogVisible(false)) },
                    onConfirm = { days -> onAction(SignUpAction.UpdateWeeklyHolidays(days)) }
                )
            }

            if (uiState.operation.showMonthlyWeekDialog) {
                MonthlyWeekDialog(
                    selectedWeeks = uiState.operation.monthlyHolidayWeeks,
                    onDismiss = { onAction(SignUpAction.SetMonthlyWeekDialogVisible(false)) },
                    onConfirm = { weeks -> onAction(SignUpAction.UpdateMonthlyWeeks(weeks)) }
                )
            }
            if (uiState.operation.showMonthlyDayDialog) {
                WeeklyHolidayDialog(
                    selectedDays = uiState.operation.monthlyHolidayDays,
                    onDismiss = { onAction(SignUpAction.SetMonthlyDayDialogVisible(false)) },
                    onConfirm = { days -> onAction(SignUpAction.UpdateMonthlyDays(days)) }
                )
            }


            // ★ DateRangePicker Logic
            if (uiState.operation.showTempHolidayDatePicker) {
                val datePickerState = rememberDateRangePickerState()

                DatePickerDialog(
                    onDismissRequest = { onAction(SignUpAction.SetTempHolidayDatePickerVisible(false)) },
                    tonalElevation = 0.dp,
                    confirmButton = {
                        TextButton(onClick = {
                            val startStr = millisToDate(datePickerState.selectedStartDateMillis)
                            val endStr = millisToDate(datePickerState.selectedEndDateMillis)
                            val finalEndStr = if (endStr.isEmpty()) startStr else endStr
                            if (startStr.isNotEmpty()) onAction(SignUpAction.UpdateTempHolidayRange(startStr, finalEndStr))
                        }) {
                            Text("저장", color = PointRed, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = DatePickerDefaults.colors(
                        containerColor = White
                    )
                ) {
                    DateRangePicker(
                        state = datePickerState,
                        colors = DatePickerDefaults.colors(
                            containerColor = White,
                            titleContentColor = SubBlack,
                            headlineContentColor = SubBlack,
                            subheadContentColor = SubBlack,
                            weekdayContentColor = SubBlack,
                            yearContentColor = SubGray,
                            currentYearContentColor = PointRed,
                            selectedYearContentColor = White,
                            selectedYearContainerColor = PointRed,
                            dayContentColor = SubBlack,
                            disabledDayContentColor = SubLightGray,
                            selectedDayContentColor = White,
                            selectedDayContainerColor = PointRed,
                            todayContentColor = PointRed,
                            todayDateBorderColor = PointRed,
                            dayInSelectionRangeContainerColor = PointRed.copy(alpha = 0.1f),
                            dayInSelectionRangeContentColor = SubBlack
                        )
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "Step 4 (매주 선택)", heightDp = 800)
@Composable
fun PreviewStep4WeeklySelected() {
    SeatNowTheme {
        Step4OperatingScreen(
            uiState = OwnerSignUpUiState(
                operation = OperationState(regularHolidayType = 1) // ★ Preview 마이그레이션
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
            uiState = OwnerSignUpUiState(
                operation = OperationState(regularHolidayType = 2) // ★ Preview 마이그레이션
            ),
            onAction = {}
        )
    }
}