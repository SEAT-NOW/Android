package com.gmg.seatnow.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commandiron.wheel_picker_compose.core.WheelPickerDefaults
import com.commandiron.wheel_picker_compose.core.WheelTextPicker
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SubBlack
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.SubLightGray
import com.gmg.seatnow.presentation.theme.SubPaleGray
import com.gmg.seatnow.presentation.theme.White

@Composable
fun SeatNowDateBox(
    dateText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.Companion,
    borderColor: Color = PointRed,
    textColor: Color = PointRed,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .height(24.dp)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .then(
                if (enabled) Modifier.Companion.clickable(onClick = onClick) else Modifier.Companion
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Companion.CenterStart
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Companion.CenterVertically,
            modifier = Modifier.Companion.fillMaxWidth()
        ) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium.copy(color = textColor)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = borderColor,
                modifier = Modifier.Companion.size(30.dp)
            )
        }
    }
}

// [보조 컴포넌트] 시간 텍스트 (밑줄 포함)
@Composable
fun TimeDisplayBox(
    hour: Int,
    minute: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    width: Dp = 85.dp,
    fontSize: TextUnit = 20.sp
) {
    Column(
        modifier = Modifier.Companion
            .width(width) // ★ 반응형 너비
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.Companion.CenterVertically) {
            Text(
                text = "%02d:%02d".format(hour, minute),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Companion.Bold,
                    fontSize = fontSize // ★ 반응형 폰트
                ),
                color = SubBlack
            )
            Spacer(modifier = Modifier.Companion.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.Companion
                    .rotate(if (isSelected) 180f else 0f)
                    .size(if (fontSize < 18.sp) 20.dp else 24.dp), // 아이콘 크기도 조정
                tint = if (isSelected) SubBlack else SubLightGray
            )
        }
        Spacer(modifier = Modifier.Companion.height(4.dp))

        HorizontalDivider(
            thickness = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) SubBlack else SubLightGray
        )
    }
}

@Composable
fun WeeklyHolidayDialog(
    selectedDays: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedDays) }
    val daysText = listOf("일", "월", "화", "수", "목", "금", "토")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White, // ★ [수정 1] 배경 완전 흰색
        tonalElevation = 0.dp,  // ★ [수정 1] 틴트(분홍끼) 제거
        title = {
            Box(modifier = Modifier.Companion.fillMaxWidth()) {
                Text(
                    text = "휴무 요일",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Companion.Bold),
                    modifier = Modifier.Companion.align(Alignment.Companion.Center) // 텍스트 정중앙
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = SubGray, // 아이콘 회색
                    modifier = Modifier.Companion
                        .align(Alignment.Companion.CenterEnd) // 아이콘 우측 끝
                        .size(20.dp)
                        .clickable { onDismiss() }
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.Companion.fillMaxWidth(), // ★ 가로 채우기
                horizontalAlignment = Alignment.Companion.CenterHorizontally
            ) {

                HorizontalDivider(color = SubPaleGray, thickness = 1.dp)
                Spacer(modifier = Modifier.Companion.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween, // ★ 간격 균등 배치
                    modifier = Modifier.Companion.fillMaxWidth()
                ) {
                    daysText.forEachIndexed { index, text ->
                        val isSelected = tempSelected.contains(index)

                        // ★ 디자인 수정: 선택됨(Red BG/White Text), 미선택(White BG/Gray Text/Gray Border)
                        val bgColor = if (isSelected) PointRed else White
                        val contentColor = if (isSelected) White else PointRed
                        val borderColor = PointRed

                        Box(
                            modifier = Modifier.Companion
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .border(1.dp, borderColor, CircleShape)
                                .clickable {
                                    tempSelected =
                                        if (isSelected) tempSelected - index else tempSelected + index
                                },
                            contentAlignment = Alignment.Companion.Center
                        ) {
                            Text(
                                text = text,
                                color = contentColor,
                                fontWeight = if (isSelected) FontWeight.Companion.Bold else FontWeight.Companion.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(tempSelected) },
                colors = ButtonDefaults.buttonColors(containerColor = PointRed),
                modifier = Modifier.Companion.fillMaxWidth().height(40.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            ) {
                Text("완료", style = MaterialTheme.typography.labelSmall)
            }
        }
    )
}

@Composable
fun MonthlyWeekDialog(
    selectedWeeks: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedWeeks) }
    val weeks = listOf(1, 2, 3, 4, 5)


    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White, // ★ [수정 1] 배경 완전 흰색
        tonalElevation = 0.dp,  // ★ [수정 1] 틴트 제거
        title = {
            Box(modifier = Modifier.Companion.fillMaxWidth()) {
                Text(
                    text = "휴무 주차",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Companion.Bold),
                    modifier = Modifier.Companion.align(Alignment.Companion.Center) // 텍스트 정중앙
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = SubGray, // 아이콘 회색
                    modifier = Modifier.Companion
                        .align(Alignment.Companion.CenterEnd) // 아이콘 우측 끝
                        .size(20.dp)
                        .clickable { onDismiss() }
                )
            }
        },
        text = {
            Column(modifier = Modifier.Companion.fillMaxWidth()) {
                weeks.forEach { week ->
                    val isSelected = tempSelected.contains(week)
                    val label = if (week == 5) "마지막 주" else "${week}주"

                    Column {
                        Box(
                            modifier = Modifier.Companion
                                .fillMaxWidth()
                                .height(42.dp)
                                .clickable {
                                    tempSelected =
                                        if (isSelected) tempSelected - week else tempSelected + week
                                }
                        ) {
                            // 텍스트 중앙 정렬
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) PointRed else SubGray,
                                fontWeight = if (isSelected) FontWeight.Companion.Bold else FontWeight.Companion.Normal,
                                modifier = Modifier.Companion.align(Alignment.Companion.Center)
                            )

                            // 체크 아이콘 우측 끝 정렬 (텍스트 위치에 영향 안 줌)
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PointRed,
                                    modifier = Modifier.Companion
                                        .align(Alignment.Companion.CenterEnd) // 우측 끝 정렬
                                        .padding(end = 8.dp) // 우측 여백
                                        .size(16.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = SubPaleGray, thickness = 1.dp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(tempSelected) },
                colors = ButtonDefaults.buttonColors(containerColor = PointRed),
                modifier = Modifier.Companion.fillMaxWidth().height(40.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            ) {
                Text("완료", style = MaterialTheme.typography.labelSmall)
            }
        }
    )
}

@Composable
fun SingleDayDialog(
    selectedDay: Int, // 0~6
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var tempSelected by remember { mutableIntStateOf(selectedDay) }
    val daysText = listOf("일", "월", "화", "수", "목", "금", "토")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        tonalElevation = 0.dp,
        title = {
            Box(modifier = Modifier.Companion.fillMaxWidth()) {
                Text(
                    "휴무 요일",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Companion.Bold),
                    modifier = Modifier.Companion.align(Alignment.Companion.Center)
                )
                Icon(
                    Icons.Default.Close,
                    "닫기",
                    tint = SubGray,
                    modifier = Modifier.Companion.align(Alignment.Companion.CenterEnd)
                        .clickable { onDismiss() })
            }
        },
        text = {
            Column(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalAlignment = Alignment.Companion.CenterHorizontally
            ) {
                HorizontalDivider(color = SubPaleGray, thickness = 1.dp)
                Spacer(modifier = Modifier.Companion.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.Companion.fillMaxWidth()
                ) {
                    daysText.forEachIndexed { index, text ->
                        val isSelected = tempSelected == index // 단일 선택 비교
                        val bgColor = if (isSelected) PointRed else White
                        val contentColor = if (isSelected) White else PointRed
                        val borderColor = PointRed // 선택 안되도 테두리는 빨강으로 유지(WeeklyDialog와 통일감)

                        Box(
                            modifier = Modifier.Companion
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .border(1.dp, borderColor, CircleShape)
                                .clickable { tempSelected = index }, // 클릭 시 바로 선택 변경
                            contentAlignment = Alignment.Companion.Center
                        ) {
                            Text(
                                text,
                                color = contentColor,
                                fontWeight = if (isSelected) FontWeight.Companion.Bold else FontWeight.Companion.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(tempSelected) },
                colors = ButtonDefaults.buttonColors(containerColor = PointRed),
                modifier = Modifier.Companion.fillMaxWidth().height(40.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            ) {
                Text("완료", style = MaterialTheme.typography.labelSmall)
            }
        }
    )
}

@Composable
fun SeatNowTimePicker(
    hour: Int,
    minute: Int,
    onTimeChanged: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hours = remember { (0..23).map { "%02d".format(it) } }
    val minutes = remember { (0..55 step 5).map { "%02d".format(it) } }
    val hourIndex = hours.indexOf("%02d".format(hour)).coerceAtLeast(0)
    val minuteIndex = minutes.indexOf("%02d".format(minute)).coerceAtLeast(0)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            key(hour) {
                WheelTextPicker(
                    texts = hours,
                    rowCount = 3,
                    size = DpSize(70.dp, 120.dp),
                    startIndex = hourIndex,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = SubBlack,
                    selectorProperties = WheelPickerDefaults.selectorProperties(enabled = false),
                    onScrollFinished = { snappedIndex ->
                        val newHour = hours[snappedIndex].toInt()
                        onTimeChanged(newHour, minute)
                        return@WheelTextPicker null
                    }
                )
            }
            Text(text = ":", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp), modifier = Modifier.padding(horizontal = 8.dp))
            key(minute) {
                WheelTextPicker(
                    texts = minutes,
                    rowCount = 3,
                    size = DpSize(70.dp, 120.dp),
                    startIndex = minuteIndex,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = SubBlack,
                    selectorProperties = WheelPickerDefaults.selectorProperties(enabled = false),
                    onScrollFinished = { snappedIndex ->
                        val newMinute = minutes[snappedIndex].toInt()
                        onTimeChanged(hour, newMinute)
                        return@WheelTextPicker null
                    }
                )
            }
        }
    }
}

@Composable
fun DayOfWeekSelector(
    selectedDays: Set<Int>,
    disabledDays: Set<Int> = emptySet(),
    buttonSize: Dp = 40.dp,
    onDayClick: (Int) -> Unit
) {
    val days = listOf("일", "월", "화", "수", "목", "금", "토")
    val fontSize = if(buttonSize < 40.dp) 12.sp else 14.sp
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        days.forEachIndexed { index, dayName ->
            val isSelected = selectedDays.contains(index)
            val isDisabled = disabledDays.contains(index)
            val backgroundColor = if (isDisabled) SubLightGray else if (isSelected) PointRed else White
            val contentColor = if (isDisabled) White else if (isSelected) White else PointRed
            val borderColor = if (isDisabled || isSelected) Color.Transparent else PointRed

            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .border(1.dp, borderColor, CircleShape)
                    .clickable(enabled = !isDisabled) { onDayClick(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = dayName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = fontSize), color = contentColor)
            }
        }
    }
}