package com.gmg.seatnow.presentation.owner.store.mypage.storeManage.storeManageEdit

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.component.*
import com.gmg.seatnow.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =============================================================================
// [Tab 1] 운영 정보 탭 내용
// =============================================================================
@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabContentOperationInfo(
    uiState: StoreEditMainViewModel.StoreEditUiState,
    onAction: (StoreEditAction) -> Unit
) {
    val daysText = listOf("일", "월", "화", "수", "목", "금", "토")

    fun formatDays(indices: Set<Int>): String =
        if (indices.isEmpty()) "요일 선택" else indices.sorted().joinToString(" · ") { daysText[it] }

    // 화면 표시용 (10 -> 마지막)
    fun formatWeeks(indices: Set<Int>): String {
        if (indices.isEmpty()) return "주 선택"
        return indices.sorted().joinToString(" · ") { week ->
            if (week == 10) "마지막" else "$week"
        } + " 주"
    }

    fun millisToDate(millis: Long?): String =
        if (millis != null) SimpleDateFormat("yyyy/MM/dd", Locale.KOREA).format(Date(millis)) else ""

    var expandedScheduleId by remember { mutableStateOf<Long?>(null) }
    var expandedTimeTarget by remember { mutableStateOf(TimeTarget.None) }

    val disabledOperatingDays = if (uiState.regularHolidayType == 1) uiState.weeklyHolidayDays else emptySet()
    val scheduledDays = uiState.operatingSchedules.flatMap { it.selectedDays }.toSet()
    val isWeekFull = (disabledOperatingDays + scheduledDays).size >= 7

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isSmallScreen = maxWidth < 380.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(White),
            contentPadding = PaddingValues(24.dp)
        ) {
            item {
                Text(
                    text = "정기 휴무일",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = SubBlack
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 1-1. 매주
                val isWeeklySelected = uiState.regularHolidayType == 1
                val weeklyTextColor = if (isWeeklySelected) PointRed else SubGray
                val weeklyBorderColor = if (isWeeklySelected) PointRed else SubLightGray

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    SeatNowCheckRadioButton(
                        selected = isWeeklySelected,
                        onClick = { onAction(StoreEditAction.ToggleRegularHolidayType(1)) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("매주", style = MaterialTheme.typography.bodyMedium, color = weeklyTextColor)
                    Spacer(modifier = Modifier.width(12.dp))

                    val displayWeeklyText = formatDays(uiState.weeklyHolidayDays)
                    SeatNowDropdownButton(
                        text = displayWeeklyText,
                        onClick = { onAction(StoreEditAction.SetWeeklyDialogVisible(true)) },
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

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    SeatNowCheckRadioButton(
                        selected = isMonthlySelected,
                        onClick = { onAction(StoreEditAction.ToggleRegularHolidayType(2)) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("매월", style = MaterialTheme.typography.bodyMedium, color = monthlyTextColor)
                    Spacer(modifier = Modifier.width(12.dp))

                    val displayMonthlyWeekText = formatWeeks(uiState.monthlyHolidayWeeks)
                    SeatNowDropdownButton(
                        text = displayMonthlyWeekText,
                        onClick = { onAction(StoreEditAction.SetMonthlyWeekDialogVisible(true)) },
                        enabled = isMonthlySelected,
                        modifier = Modifier.widthIn(min = 60.dp),
                        borderColor = monthlyBorderColor,
                        textColor = monthlyTextColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    val displayMonthlyDayText = formatDays(uiState.monthlyHolidayDays)
                    SeatNowDropdownButton(
                        text = displayMonthlyDayText,
                        onClick = { onAction(StoreEditAction.SetMonthlyDayDialogVisible(true)) },
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

                // --- 2. 임시 휴무 ---
                Text(
                    text = "임시 휴무",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = SubBlack
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SeatNowCheckRadioButton(
                        selected = uiState.isTempHolidayEnabled,
                        onClick = { onAction(StoreEditAction.ToggleTempHoliday) },
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    val tempHolidayBorderColor = if (uiState.isTempHolidayEnabled) PointRed else SubLightGray
                    val tempHolidayTextColor = if (uiState.isTempHolidayEnabled) PointRed else SubGray

                    SeatNowDateBox(
                        dateText = if (uiState.tempHolidayStart.isNotEmpty()) uiState.tempHolidayStart else "YYYY/MM/DD",
                        onClick = { if (uiState.isTempHolidayEnabled) onAction(StoreEditAction.SetTempHolidayDatePickerVisible(true)) },
                        enabled = uiState.isTempHolidayEnabled,
                        modifier = Modifier.widthIn(max = 120.dp),
                        borderColor = tempHolidayBorderColor,
                        textColor = tempHolidayTextColor
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("~", style = MaterialTheme.typography.titleMedium, color = SubBlack)
                    Spacer(modifier = Modifier.width(12.dp))
                    SeatNowDateBox(
                        dateText = if (uiState.tempHolidayEnd.isNotEmpty()) uiState.tempHolidayEnd else "YYYY/MM/DD",
                        onClick = { if (uiState.isTempHolidayEnabled) onAction(StoreEditAction.SetTempHolidayDatePickerVisible(true)) },
                        enabled = uiState.isTempHolidayEnabled,
                        modifier = Modifier.widthIn(max = 120.dp),
                        borderColor = tempHolidayBorderColor,
                        textColor = tempHolidayTextColor
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(thickness = 1.dp, color = SubLightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(24.dp))

                // --- 3. 운영 정보 ---
                Text(
                    text = "운영 정보",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = SubBlack
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 운영 스케줄 리스트
            items(uiState.operatingSchedules.size) { index ->
                val schedule = uiState.operatingSchedules[index]
                Column(modifier = Modifier.fillMaxWidth()) {
                    DayOfWeekSelector(
                        selectedDays = schedule.selectedDays,
                        disabledDays = disabledOperatingDays,
                        buttonSize = if (isSmallScreen) 34.dp else 40.dp,
                        onDayClick = { dayIdx -> onAction(StoreEditAction.UpdateOperatingDays(schedule.id, dayIdx)) }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    val isMyStartOpen = (expandedScheduleId == schedule.id && expandedTimeTarget == TimeTarget.Start)
                    val isMyEndOpen = (expandedScheduleId == schedule.id && expandedTimeTarget == TimeTarget.End)

                    OperatingScheduleItemRow(
                        schedule = schedule,
                        isDeleteEnabled = uiState.operatingSchedules.size > 1,
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
                        onUpdateStart = { h, m -> onAction(StoreEditAction.UpdateOperatingTime(schedule.id, h, m, schedule.endHour, schedule.endMin)) },
                        onUpdateEnd = { h, m -> onAction(StoreEditAction.UpdateOperatingTime(schedule.id, schedule.startHour, schedule.startMin, h, m)) },
                        onDelete = { onAction(StoreEditAction.RemoveOperatingSchedule(schedule.id)) }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                if (index < uiState.operatingSchedules.size - 1) {
                    HorizontalDivider(thickness = 1.dp, color = SubPaleGray)
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    SeatNowRedPlusButton(
                        onClick = { onAction(StoreEditAction.AddOperatingSchedule) },
                        isEnabled = !isWeekFull
                    )
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // --- 다이얼로그 호출 (SeatNowComponents.kt 활용) ---

        // 1. 매주 요일 선택
        if (uiState.showWeeklyDayDialog) {
            WeeklyHolidayDialog(
                selectedDays = uiState.weeklyHolidayDays,
                onDismiss = { onAction(StoreEditAction.SetWeeklyDialogVisible(false)) },
                onConfirm = { days -> onAction(StoreEditAction.UpdateWeeklyHolidays(days)) }
            )
        }

        // 2. 매월 주차 선택 (★ 핵심 수정: 값 변환)
        if (uiState.showMonthlyWeekDialog) {
            // API용 값(10) -> UI용 값(5) 변환
            val uiWeeks = uiState.monthlyHolidayWeeks.map { if (it == 10) 5 else it }.toSet()

            MonthlyWeekDialog(
                selectedWeeks = uiWeeks,
                onDismiss = { onAction(StoreEditAction.SetMonthlyWeekDialogVisible(false)) },
                onConfirm = { weeks ->
                    // UI용 값(5) -> API용 값(10) 변환
                    val apiWeeks = weeks.map { if (it == 5) 10 else it }.toSet()
                    onAction(StoreEditAction.UpdateMonthlyWeeks(apiWeeks))
                }
            )
        }

        // 3. 매월 요일 선택 (SingleDayDialog 대신 WeeklyHolidayDialog 사용)
        if (uiState.showMonthlyDayDialog) {
            WeeklyHolidayDialog(
                selectedDays = uiState.monthlyHolidayDays,
                onDismiss = { onAction(StoreEditAction.SetMonthlyDayDialogVisible(false)) },
                onConfirm = { days -> onAction(StoreEditAction.UpdateMonthlyDays(days)) }
            )
        }

        // 4. 날짜 선택기
        if (uiState.showTempHolidayDatePicker) {
            val datePickerState = rememberDateRangePickerState()
            DatePickerDialog(
                onDismissRequest = { onAction(StoreEditAction.SetTempHolidayDatePickerVisible(false)) },
                tonalElevation = 0.dp,
                confirmButton = {
                    TextButton(onClick = {
                        val startStr = millisToDate(datePickerState.selectedStartDateMillis)
                        val endStr = millisToDate(datePickerState.selectedEndDateMillis)
                        val finalEndStr = if (endStr.isEmpty()) startStr else endStr
                        if (startStr.isNotEmpty()) onAction(StoreEditAction.UpdateTempHolidayRange(startStr, finalEndStr))
                    }) {
                        Text("저장", color = PointRed, fontWeight = FontWeight.Bold)
                    }
                },
                colors = DatePickerDefaults.colors(containerColor = White)
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

// =============================================================================
// [Tab 2, 3] 더미 탭 내용
// =============================================================================
@Composable
fun TabContentMenu() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("메뉴 탭 (추후 구현)", color = SubGray)
    }
}

@Composable
fun TabContentStorePhotos() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("가게 사진 탭 (추후 구현)", color = SubGray)
    }
}