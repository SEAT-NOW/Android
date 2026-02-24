package com.gmg.seatnow.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmg.seatnow.domain.model.OperatingScheduleItem
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.SubLightGray

@Composable
fun OperatingScheduleItemRow(
    schedule: OperatingScheduleItem,
    isDeleteEnabled: Boolean,
    expandedTarget: TimeTarget, // ★ 외부에서 제어 (None, Start, End)
    isSmallScreen: Boolean = false, // ★ 반응형 플래그
    onToggleStart: () -> Unit, // ★ 클릭 시 토글 요청
    onToggleEnd: () -> Unit,   // ★ 클릭 시 토글 요청
    onUpdateStart: (Int, Int) -> Unit,
    onUpdateEnd: (Int, Int) -> Unit,
    onDelete: () -> Unit
) {
    // 화면 크기에 따른 사이즈 조정
    val iconSize = if (isSmallScreen) 20.dp else 24.dp
    val timeFontSize = if (isSmallScreen) 16.sp else 20.sp
    val tildeFontSize = if (isSmallScreen) 14.sp else 16.sp
    val iconSpacing = if (isSmallScreen) 8.dp else 12.dp
    val textSpacing = if (isSmallScreen) 8.dp else 16.dp
    val timeBoxWidth = if (isSmallScreen) 70.dp else 85.dp

    // 중앙 정렬을 위한 밸런스 여백 (삭제 아이콘 크기만큼 왼쪽도 띄워줌)
    val sideBalanceWidth = iconSize + iconSpacing

    Column(
        modifier = Modifier.Companion.fillMaxWidth(),
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.Companion.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.Companion.fillMaxWidth()
        ) {
            // 왼쪽 밸런스 여백
            Spacer(modifier = Modifier.Companion.width(sideBalanceWidth))

            Row(verticalAlignment = Alignment.Companion.CenterVertically) {
                TimeDisplayBox(
                    hour = schedule.startHour,
                    minute = schedule.startMin,
                    isSelected = expandedTarget == TimeTarget.Start,
                    onClick = onToggleStart, // ★
                    width = timeBoxWidth,
                    fontSize = timeFontSize
                )
                Spacer(modifier = Modifier.Companion.width(textSpacing))
                Text(
                    "~",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = tildeFontSize),
                    color = SubGray
                )
                Spacer(modifier = Modifier.Companion.width(textSpacing))
                TimeDisplayBox(
                    hour = schedule.endHour,
                    minute = schedule.endMin,
                    isSelected = expandedTarget == TimeTarget.End,
                    onClick = onToggleEnd, // ★
                    width = timeBoxWidth,
                    fontSize = timeFontSize
                )
            }

            Spacer(modifier = Modifier.Companion.width(iconSpacing))
            IconButton(
                onClick = onDelete,
                enabled = isDeleteEnabled,
                modifier = Modifier.Companion.size(iconSize)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = if (isDeleteEnabled) PointRed else SubLightGray
                )
            }
        }

        // 휠 피커 표시
        if (expandedTarget != TimeTarget.None) {
            Spacer(modifier = Modifier.Companion.height(16.dp))
            val currentHour =
                if (expandedTarget == TimeTarget.Start) schedule.startHour else schedule.endHour
            val currentMin =
                if (expandedTarget == TimeTarget.Start) schedule.startMin else schedule.endMin

            SeatNowTimePicker(
                hour = currentHour,
                minute = currentMin,
                onTimeChanged = { h, m ->
                    if (expandedTarget == TimeTarget.Start) onUpdateStart(h, m) else onUpdateEnd(
                        h,
                        m
                    )
                },
                modifier = Modifier.Companion.fillMaxWidth()
            )
        }
    }
}

enum class TimeTarget {
    None, Start, End
}