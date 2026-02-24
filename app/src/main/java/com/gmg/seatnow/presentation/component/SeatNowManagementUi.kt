package com.gmg.seatnow.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.FloorCategory
import com.gmg.seatnow.domain.model.TableItem
import com.gmg.seatnow.presentation.owner.store.seat.SeatManagementViewModel
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SubBlack
import com.gmg.seatnow.presentation.theme.SubDarkGray
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.SubLightGray
import com.gmg.seatnow.presentation.theme.White

@Composable
fun SpaceItemCard(
    name: String,
    seatCount: Int,
    isSelected: Boolean,
    onItemClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isDeleteEnabled: Boolean = true
) {
    val contentColor = if (isSelected) PointRed else SubDarkGray
    val borderColor = if (isSelected) PointRed else SubLightGray
    val deleteIconColor = if (isDeleteEnabled) PointRed else SubLightGray
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onItemClick
            ),
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {
        Row(
            modifier = Modifier.Companion
                .weight(1f)
                .fillMaxHeight()
                .border(
                    1.dp,
                    borderColor,
                    androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                )
                .background(White, androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.Companion.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Companion.Bold),
                color = contentColor
            )
            Text(
                text = "${seatCount}석",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Companion.Bold),
                color = contentColor
            )
        }
        Spacer(modifier = Modifier.Companion.width(12.dp))
        IconButton(onClick = onEditClick, modifier = Modifier.Companion.size(24.dp)) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "수정", tint = SubDarkGray)
        }
        Spacer(modifier = Modifier.Companion.width(12.dp))
        IconButton(
            onClick = onDeleteClick,
            enabled = isDeleteEnabled,
            modifier = Modifier.Companion.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "삭제",
                tint = deleteIconColor
            )
        }
    }
}

@Composable
fun TableItemCard(
    nValue: String,
    mValue: String,
    onNChange: (String) -> Unit,
    onMChange: (String) -> Unit,
    onDeleteClick: () -> Unit,
    isDeleteEnabled: Boolean = true,
    isEnabled: Boolean = true
) {
    val iconColor = if (isEnabled) PointRed else SubGray
    Row(
        modifier = Modifier.Companion.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.Companion.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularNumberField(
            value = nValue,
            onValueChange = onNChange,
            placeholder = "N",
            isEnabled = isEnabled
        )
        Spacer(modifier = Modifier.Companion.width(8.dp))
        Text(
            text = "인 테이블",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isEnabled) SubBlack else SubGray
        )
        Spacer(modifier = Modifier.Companion.width(16.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_table_multiply),
            contentDescription = "multiply",
            tint = iconColor,
            modifier = Modifier.Companion.size(12.dp)
        )
        Spacer(modifier = Modifier.Companion.width(16.dp))
        CircularNumberField(
            value = mValue,
            onValueChange = onMChange,
            placeholder = "M",
            isEnabled = isEnabled
        )
        Spacer(modifier = Modifier.Companion.width(8.dp))
        Text(
            text = "개",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isEnabled) SubBlack else SubGray
        )
        Spacer(modifier = Modifier.Companion.width(24.dp))
        IconButton(
            onClick = onDeleteClick,
            enabled = isDeleteEnabled && isEnabled,
            modifier = Modifier.Companion.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "삭제",
                tint = if (isDeleteEnabled && isEnabled) PointRed else SubLightGray
            )
        }
    }
}

@Composable
fun SeatHeaderSection(
    currentMode: SeatManagementViewModel.SeatDisplayMode,
    onModeChange: (SeatManagementViewModel.SeatDisplayMode) -> Unit
) {
    Row(
        modifier = Modifier.Companion.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {
        // 왼쪽 타이틀
        Text(
            text = "실시간 좌석 관리",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Companion.Bold),
            color = SubBlack
        )

        // 오른쪽: 애니메이션이 적용된 커스텀 토글
        AnimatedSeatToggle(
            currentMode = currentMode,
            onModeChange = onModeChange
        )
    }
}

@Composable
private fun AnimatedSeatToggle(
    currentMode: SeatManagementViewModel.SeatDisplayMode,
    onModeChange: (SeatManagementViewModel.SeatDisplayMode) -> Unit
) {
    val containerHeight = 24.dp // 디자인 비율에 맞춘 높이
    val containerWidth = 113.dp // 전체 너비
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(50) // 완전 둥근 캡슐 모양

    // 1. 애니메이션 상태 정의
    // 이용 좌석(OCCUPIED)일 때 true
    val isOccupied = currentMode == SeatManagementViewModel.SeatDisplayMode.OCCUPIED

    // [핵심] 텍스트 색상 애니메이션 (부드럽게 전환)
    val emptyTextColor by animateColorAsState(
        targetValue = if (isOccupied) PointRed else Color.Companion.White,
        label = "EmptyText"
    )
    val occupiedTextColor by animateColorAsState(
        targetValue = if (isOccupied) Color.Companion.White else PointRed,
        label = "OccupiedText"
    )

    // [핵심] 배경 알약 이동 애니메이션 (Alignment를 이용해 좌우 슬라이딩)
    // Bias: -1(왼쪽), 1(오른쪽) -> 이를 부드럽게 전환
    val alignmentBias by animateFloatAsState(
        targetValue = if (isOccupied) 1f else -1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ), // 300ms 동안 쫀득하게 이동
        label = "Slide"
    )

    Box(
        modifier = Modifier.Companion
            .width(containerWidth)
            .height(containerHeight)
            .border(1.dp, PointRed, shape) // XML 디자인과 동일한 빨간 테두리
            .clip(shape)
            .background(Color.Companion.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // 물결 효과 제거
            ) {
                // 클릭 시 모드 반전
                val newMode = if (isOccupied) SeatManagementViewModel.SeatDisplayMode.EMPTY
                else SeatManagementViewModel.SeatDisplayMode.OCCUPIED
                onModeChange(newMode)
            }
    ) {
        // 2. 움직이는 배경 (빨간색 알약)
        // 전체 Box 안에서 alignmentBias에 따라 좌우로 움직임
        Box(
            modifier = Modifier.Companion
                .fillMaxHeight()
                .fillMaxWidth(0.5f) // 정확히 절반 크기
                .align(
                    BiasAlignment(
                        horizontalBias = alignmentBias,
                        verticalBias = 0f
                    )
                ) // 애니메이션 적용된 정렬
                .background(PointRed, shape) // XML 색상 적용
        )

        // 3. 텍스트 레이어 (배경 위에 겹쳐짐)
        Row(modifier = Modifier.Companion.fillMaxSize()) {
            // 빈 좌석 텍스트 (왼쪽)
            Box(
                contentAlignment = Alignment.Companion.Center,
                modifier = Modifier.Companion.weight(1f).fillMaxHeight()
            ) {
                Text(
                    text = "빈 좌석",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Companion.Bold),
                    color = emptyTextColor // 색상 애니메이션 적용
                )
            }

            // 이용 좌석 텍스트 (오른쪽)
            Box(
                contentAlignment = Alignment.Companion.Center,
                modifier = Modifier.Companion.weight(1f).fillMaxHeight()
            ) {
                Text(
                    text = "이용 좌석",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Companion.Bold),
                    color = occupiedTextColor // 색상 애니메이션 적용
                )
            }
        }
    }
}


@Composable
fun FloorFilterRow(
    categories: List<FloorCategory>,
    selectedId: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category.id == selectedId
            val bgColor = if (isSelected) PointRed else White
            val textColor = if (isSelected) White else PointRed
            val borderColor = PointRed

            Box(
                modifier = Modifier.Companion
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .border(
                        1.dp,
                        borderColor,
                        androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelect(category.id) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Companion.Center
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Companion.Bold else FontWeight.Companion.Medium),
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun SeatStatusSummary(
    mode: SeatManagementViewModel.SeatDisplayMode,
    totalSeats: Int,
    usedSeats: Int
) {
    // 1. 표시할 텍스트 및 숫자 계산
    // 모드가 '빈 좌석' 보기여도, 태그 로직은 '이용 좌석' 비율 기준이므로 usedSeats/totalSeats로 계산합니다.
    val targetCount = if (mode == SeatManagementViewModel.SeatDisplayMode.EMPTY) {
        totalSeats - usedSeats // 빈 좌석 수
    } else {
        usedSeats // 이용 좌석 수
    }

    val labelText = if (mode == SeatManagementViewModel.SeatDisplayMode.EMPTY) {
        "빈 좌석 수/전체 좌석 수"
    } else {
        "이용 좌석 수/전체 좌석 수"
    }

    // 2. [핵심 로직] 점유율에 따른 태그 리소스 결정
    // 비율 계산 (0 ~ 100)
    val usagePercentage = if (totalSeats == 0) 0f else (usedSeats.toFloat() / totalSeats.toFloat()) * 100f

    val tagResId = when {
        usagePercentage >= 100f -> R.drawable.tag_full    // 100% : 만석
        usagePercentage >= 67f -> R.drawable.tag_hard     // 67% ~ 99% : 혼잡
        usagePercentage >= 34f -> R.drawable.tag_normal   // 34% ~ 66% : 보통
        else -> R.drawable.tag_spare                      // 0% ~ 33% : 여유
    }

    // 접근성 설명을 위한 텍스트
    val contentDesc = when {
        usagePercentage >= 100f -> "만석"
        usagePercentage >= 67f -> "혼잡"
        usagePercentage >= 34f -> "보통"
        else -> "여유"
    }

    Row(
        modifier = Modifier.Companion.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {
        // 왼쪽: 라벨 (빈 좌석 수/전체 좌석 수)
        Text(
            text = labelText,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Companion.Bold),
            color = SubBlack
        )

        // 오른쪽: 수치 및 태그 이미지
        Row(verticalAlignment = Alignment.Companion.CenterVertically) {
            Text(
                text = "${targetCount}/${totalSeats}석",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Companion.Bold),
                color = SubBlack
            )

            Spacer(modifier = Modifier.Companion.width(8.dp))

            // [수정됨] 기존 Box(Text)를 제거하고 Image 컴포넌트로 XML 리소스 렌더링
            Image(
                painter = painterResource(id = tagResId),
                contentDescription = contentDesc,
                modifier = Modifier.Companion.height(20.dp), // XML 원본 높이(18dp)와 유사하게 조정
                contentScale = ContentScale.Companion.Fit
            )
        }
    }
}

@Composable
fun TableViewItem(
    item: TableItem
) {
    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(vertical = 12.dp), // 상하 여백 적절히
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {
        // 테이블 이름 (왼쪽)
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Companion.Medium),
            color = SubBlack
        )

        // 개수 표시 (오른쪽, 예: "2개")
        Text(
            text = "${item.currentCount}개",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Companion.Bold),
            color = SubBlack
        )
    }
}

@Composable
fun TableStepperItem(
    item: TableItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        modifier = Modifier.Companion.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {
        // 테이블 이름
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Companion.Medium),
            color = SubBlack
        )

        // Stepper Control
        Row(
            verticalAlignment = Alignment.Companion.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Minus Button
            IconButton(
                onClick = onDecrement,
                modifier = Modifier.Companion.size(24.dp)
            ) {
                // 아이콘 리소스를 직접 쓰거나 VectorIcon 사용 (여기선 심플하게 구현)
                Icon(
                    painter = painterResource(id = R.drawable.ic_minus), // 리소스 필요 (없으면 텍스트로 대체 가능)
                    contentDescription = "감소",
                    tint = SubBlack
                )
            }

            // Count Circle
            Box(
                modifier = Modifier.Companion
                    .size(40.dp)
                    .border(1.dp, PointRed, CircleShape),
                contentAlignment = Alignment.Companion.Center
            ) {
                Text(
                    text = "${item.currentCount}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Companion.Bold),
                    color = PointRed
                )
            }

            // Plus Button
            IconButton(
                onClick = onIncrement,
                modifier = Modifier.Companion.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_plus),
                    contentDescription = "증가",
                    tint = SubBlack
                )
            }
        }
    }
}