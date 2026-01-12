package com.gmg.seatnow.presentation.component

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.commandiron.wheel_picker_compose.core.WheelPickerDefaults
import com.commandiron.wheel_picker_compose.core.WheelTextPicker
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.FloorCategory
import com.gmg.seatnow.presentation.extension.bottomShadow
import com.gmg.seatnow.domain.model.OperatingScheduleItem
import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.model.TableItem
import com.gmg.seatnow.presentation.owner.store.seat.SeatManagementViewModel
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.presentation.util.MapUtils.createMarkerBitmap
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationSource
import com.naver.maps.map.compose.CameraPositionState
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.MapEffect
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.overlay.OverlayImage

// ★ 병합된 통합 텍스트 필드
@Composable
fun SeatNowTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height : Dp = 52.dp,
    isEnabled: Boolean = true,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isPassword: Boolean = false,
    errorText: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val backgroundColor = if (isEnabled) White else SubLightGray
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val finalKeyboardType = if (isPassword) KeyboardType.Password else keyboardType

    val borderColor = if (errorText != null) Color.Red
    else if (isFocused) SubBlack
    else SubLightGray

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .bottomShadow(offsetY = 2.dp, shadowBlurRadius = 4.dp, alpha = 0.15f, cornersRadius = 12.dp)
                .background(color = backgroundColor, shape = RoundedCornerShape(12.dp))
                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp)),
            placeholder = {
                if (!isFocused) {
                    Text(text = placeholder, color = SubLightGray, style = MaterialTheme.typography.bodyMedium)
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = errorText != null,
            readOnly = readOnly,
            enabled = isEnabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedTextColor = SubBlack,
                unfocusedTextColor = SubBlack,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent
            ),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = finalKeyboardType, imeAction = imeAction)
        )
        if (errorText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = errorText, style = MaterialTheme.typography.labelSmall.copy(color = Color.Red), modifier = Modifier.padding(start = 4.dp))
        }
    }
}

// 버튼이 포함된 텍스트 필드
@Composable
fun SignUpTextFieldWithButton(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    buttonText: String,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp,
    isEnabled: Boolean = true,
    isButtonEnabled: Boolean = true,
    timerText: String? = null,
    errorText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onButtonClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = if (errorText != null) Color.Red else if (isFocused) SubBlack else SubLightGray

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .bottomShadow(offsetY = 2.dp, shadowBlurRadius = 4.dp, alpha = 0.15f, cornersRadius = 12.dp)
                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
                .background(White, RoundedCornerShape(12.dp))
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = isEnabled,
                modifier = Modifier.weight(1f),
                interactionSource = interactionSource,
                placeholder = {
                    if (!isFocused) {
                        Text(text = placeholder, color = SubLightGray, style = MaterialTheme.typography.bodyMedium)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = visualTransformation,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                    disabledTextColor = SubGray
                ),
                singleLine = true,
                isError = errorText != null
            )
            if (timerText != null) {
                Text(text = timerText, color = SubGray, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(end = 8.dp))
            }
            Button(
                onClick = onButtonClick,
                enabled = isButtonEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PointRed,
                    contentColor = White,
                    disabledContainerColor = SubLightGray,
                    disabledContentColor = White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(text = buttonText, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
        }
        if (errorText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = errorText, style = MaterialTheme.typography.labelSmall.copy(color = Color.Red), modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Composable
fun TermItem(
    title: String,
    isChecked: Boolean,
    showArrow: Boolean,
    onToggle: () -> Unit,
    onDetailClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 6.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (isChecked) PointRed else SubLightGray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, style = Body2_Regular_14, color = SubDarkGray)
        }
        if (showArrow) {
            IconButton(onClick = onDetailClick, modifier = Modifier.size(24.dp)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "상세보기", tint = SubDarkGray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatNowTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    topMargin: Dp = 0.dp
) {
    Column(modifier = modifier.padding(top = topMargin)) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.offset(x = (-6).dp)
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "뒤로가기", modifier = Modifier.size(32.dp))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = White,
                scrolledContainerColor = White,
                navigationIconContentColor = SubBlack,
                titleContentColor = SubBlack
            )
        )
    }
}

@Composable
fun CircularNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val mainColor = if(isEnabled) PointRed else SubGray

    Box(
        modifier = modifier
            .size(48.dp)
            .border(2.dp, mainColor, CircleShape)
            .background(Color.Transparent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (value.isEmpty() && !isFocused) {
            Text(text = placeholder, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = mainColor, textAlign = TextAlign.Center))
        }
        BasicTextField(
            value = value,
            onValueChange = {
                if (it.all { char -> char.isDigit() } && it.length <= 2) {
                    onValueChange(it)
                }
            },
            enabled = isEnabled,
            interactionSource = interactionSource,
            textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SubBlack, textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            cursorBrush = SolidColor(mainColor),
            modifier = Modifier.wrapContentSize()
        )
    }
}

@Composable
fun SeatNowRedPlusButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.width(100.dp).height(40.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if(isEnabled) PointRed else SubGray, contentColor = White),
        enabled = isEnabled,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "추가", modifier = Modifier.size(24.dp))
    }
}

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
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onItemClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                .background(White, RoundedCornerShape(4.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = contentColor)
            Text(text = "${seatCount}석", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = contentColor)
        }
        Spacer(modifier = Modifier.width(12.dp))
        IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "수정", tint = SubDarkGray)
        }
        Spacer(modifier = Modifier.width(12.dp))
        IconButton(onClick = onDeleteClick, enabled = isDeleteEnabled, modifier = Modifier.size(24.dp)) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "삭제", tint = deleteIconColor)
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularNumberField(value = nValue, onValueChange = onNChange, placeholder = "N", isEnabled = isEnabled)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "인 테이블", style = MaterialTheme.typography.bodyMedium, color = if(isEnabled) SubBlack else SubGray)
        Spacer(modifier = Modifier.width(16.dp))
        Icon(painter = painterResource(id = R.drawable.ic_table_multiply), contentDescription = "multiply", tint = iconColor, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(16.dp))
        CircularNumberField(value = mValue, onValueChange = onMChange, placeholder = "M", isEnabled = isEnabled)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "개", style = MaterialTheme.typography.bodyMedium, color = if(isEnabled) SubBlack else SubGray)
        Spacer(modifier = Modifier.width(24.dp))
        IconButton(onClick = onDeleteClick, enabled = isDeleteEnabled && isEnabled, modifier = Modifier.size(24.dp)) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "삭제", tint = if (isDeleteEnabled && isEnabled) PointRed else SubLightGray)
        }
    }
}

@Composable
fun SeatNowDropdownButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = SubGray,
    textColor: Color = SubGray,
    enabled: Boolean = true
) {

    Box(
        modifier = modifier
            .height(24.dp)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .then(
                // enabled가 true일 때만 clickable 적용
                if (enabled) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(start = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = borderColor, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
fun SeatNowDateBox(
    dateText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = PointRed,
    textColor: Color = PointRed,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .height(24.dp)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .then(
                if (enabled) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = dateText, style = MaterialTheme.typography.bodyMedium.copy(color = textColor))
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = borderColor, modifier = Modifier.size(30.dp))
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
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
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
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
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
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 왼쪽 밸런스 여백
            Spacer(modifier = Modifier.width(sideBalanceWidth))

            Row(verticalAlignment = Alignment.CenterVertically) {
                TimeDisplayBox(
                    hour = schedule.startHour,
                    minute = schedule.startMin,
                    isSelected = expandedTarget == TimeTarget.Start,
                    onClick = onToggleStart, // ★
                    width = timeBoxWidth,
                    fontSize = timeFontSize
                )
                Spacer(modifier = Modifier.width(textSpacing))
                Text("~", style = MaterialTheme.typography.titleMedium.copy(fontSize = tildeFontSize), color = SubGray)
                Spacer(modifier = Modifier.width(textSpacing))
                TimeDisplayBox(
                    hour = schedule.endHour,
                    minute = schedule.endMin,
                    isSelected = expandedTarget == TimeTarget.End,
                    onClick = onToggleEnd, // ★
                    width = timeBoxWidth,
                    fontSize = timeFontSize
                )
            }

            Spacer(modifier = Modifier.width(iconSpacing))
            IconButton(onClick = onDelete, enabled = isDeleteEnabled, modifier = Modifier.size(iconSize)) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "삭제", tint = if (isDeleteEnabled) PointRed else SubLightGray)
            }
        }

        // 휠 피커 표시
        if (expandedTarget != TimeTarget.None) {
            Spacer(modifier = Modifier.height(16.dp))
            val currentHour = if (expandedTarget == TimeTarget.Start) schedule.startHour else schedule.endHour
            val currentMin = if (expandedTarget == TimeTarget.Start) schedule.startMin else schedule.endMin

            SeatNowTimePicker(
                hour = currentHour,
                minute = currentMin,
                onTimeChanged = { h, m ->
                    if (expandedTarget == TimeTarget.Start) onUpdateStart(h, m) else onUpdateEnd(h, m)
                },
                modifier = Modifier.fillMaxWidth()
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
    fontSize: androidx.compose.ui.unit.TextUnit = 20.sp
) {
    Column(
        modifier = Modifier
            .width(width) // ★ 반응형 너비
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "%02d:%02d".format(hour, minute),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize // ★ 반응형 폰트
                ),
                color = SubBlack
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier
                    .rotate(if (isSelected) 180f else 0f)
                    .size(if (fontSize < 18.sp) 20.dp else 24.dp), // 아이콘 크기도 조정
                tint = if (isSelected) SubBlack else SubLightGray
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        HorizontalDivider(
            thickness = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) SubBlack else SubLightGray
        )
    }
}

@Composable
fun SeatNowCheckRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp
) {
    val backgroundColor = if (selected) PointRed else White
    val borderColor = if (selected) PointRed else SubGray

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = White,
                modifier = Modifier.size(size * 0.7f)
            )
        }
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
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "휴무 요일",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Center) // 텍스트 정중앙
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = SubGray, // 아이콘 회색
                    modifier = Modifier
                        .align(Alignment.CenterEnd) // 아이콘 우측 끝
                        .size(20.dp)
                        .clickable { onDismiss() }
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(), // ★ 가로 채우기
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                HorizontalDivider(color = SubPaleGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween, // ★ 간격 균등 배치
                    modifier = Modifier.fillMaxWidth()
                ) {
                    daysText.forEachIndexed { index, text ->
                        val isSelected = tempSelected.contains(index)

                        // ★ 디자인 수정: 선택됨(Red BG/White Text), 미선택(White BG/Gray Text/Gray Border)
                        val bgColor = if (isSelected) PointRed else White
                        val contentColor = if (isSelected) White else PointRed
                        val borderColor = PointRed

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .border(1.dp, borderColor, CircleShape)
                                .clickable {
                                    tempSelected = if (isSelected) tempSelected - index else tempSelected + index
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = text,
                                color = contentColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(4.dp)
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
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "휴무 주차",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.align(Alignment.Center) // 텍스트 정중앙
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = SubGray, // 아이콘 회색
                    modifier = Modifier
                        .align(Alignment.CenterEnd) // 아이콘 우측 끝
                        .size(20.dp)
                        .clickable { onDismiss() }
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                weeks.forEach { week ->
                    val isSelected = tempSelected.contains(week)
                    val label = if (week == 5) "마지막 주" else "${week}주"

                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clickable {
                                    tempSelected = if (isSelected) tempSelected - week else tempSelected + week
                                }
                        ) {
                            // 텍스트 중앙 정렬
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isSelected) PointRed else SubGray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.align(Alignment.Center)
                            )

                            // 체크 아이콘 우측 끝 정렬 (텍스트 위치에 영향 안 줌)
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PointRed,
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd) // 우측 끝 정렬
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
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("완료", style = MaterialTheme.typography.labelSmall)
            }
        }
    )
}

@Preview(name = "요일 선택 다이얼로그 (Weekly)", showBackground = true)
@Composable
fun PreviewWeeklyHolidayDialog() {
    SeatNowTheme {
        // 배경을 어둡게 처리하여 다이얼로그가 잘 보이게 함 (실제 앱 환경 유사)
        Box(modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.5f))) {
            WeeklyHolidayDialog(
                selectedDays = setOf(1, 3), // 월, 수 선택된 상태 예시
                onDismiss = {},
                onConfirm = {}
            )
        }
    }
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
            Box(modifier = Modifier.fillMaxWidth()) {
                Text("휴무 요일", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.align(Alignment.Center))
                Icon(Icons.Default.Close, "닫기", tint = SubGray, modifier = Modifier.align(Alignment.CenterEnd).clickable { onDismiss() })
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                HorizontalDivider(color = SubPaleGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    daysText.forEachIndexed { index, text ->
                        val isSelected = tempSelected == index // 단일 선택 비교
                        val bgColor = if (isSelected) PointRed else White
                        val contentColor = if (isSelected) White else PointRed
                        val borderColor = PointRed // 선택 안되도 테두리는 빨강으로 유지(WeeklyDialog와 통일감)

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .border(1.dp, borderColor, CircleShape)
                                .clickable { tempSelected = index }, // 클릭 시 바로 선택 변경
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text, color = contentColor, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(tempSelected) },
                colors = ButtonDefaults.buttonColors(containerColor = PointRed),
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("완료", style = MaterialTheme.typography.labelSmall)
            }
        }
    )
}

@Preview(name = "주차 선택 다이얼로그 (Monthly)", showBackground = true)
@Composable
fun PreviewMonthlyWeekDialog() {
    SeatNowTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.5f))) {
            MonthlyWeekDialog(
                selectedWeeks = setOf(2, 4), // 2주, 4주 선택된 상태 예시
                onDismiss = {},
                onConfirm = {}
            )
        }
    }
}

enum class TimeTarget {
    None, Start, End
}

@Composable
fun AddPhotoButton(
    onClick: () -> Unit,
    currentCount: Int,
    maxCount: Int = 5,
    modifier: Modifier = Modifier
) {
    // [수정] X버튼 공간 확보용 패딩을 10dp -> 6dp로 축소
    Box(modifier = modifier.padding(top = 6.dp, end = 6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 5f) // 4:5 비율
                .background(SubPaleGray, RectangleShape)
                .clickable(onClick = onClick)
                .border(1.dp, SubLightGray, RectangleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "사진 추가",
                    tint = SubGray,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$currentCount / $maxCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = SubGray
                )
            }
        }
    }
}

// [Step 5] 등록된 사진 아이템 (X버튼 작게, 여백 축소)
@Composable
fun PhotoGridItem(
    uri: Uri,
    isRepresentative: Boolean,
    onRemove: () -> Unit,
    onSetRepresentative: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        val interactionSource = remember { MutableInteractionSource() }

        // 1. 실제 사진 영역
        Box(
            modifier = Modifier
                // [수정] X버튼 공간 확보용 패딩 10dp -> 6dp로 축소 (사진 크기 확보)
                .padding(top = 6.dp, end = 6.dp)
                .fillMaxWidth()
                .aspectRatio(4f / 5f)
                .background(SubLightGray, RectangleShape)
                .border(1.dp, SubLightGray, RectangleShape)
                .clickable(onClick = onSetRepresentative)
        ) {
            AsyncImage(
                model = uri,
                contentDescription = "가게 사진",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 2. 대표 라벨
            val labelBgColor = if (isRepresentative) PointRed else SubLightGray
            val labelTextColor = White

            Box(
                modifier = Modifier
                    .background(labelBgColor, RectangleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .align(Alignment.TopStart)
            ) {
                Text(
                    text = "대표",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = labelTextColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 3. 삭제 버튼 (크기 축소)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                // [수정] 버튼 크기 20dp -> 18dp로 축소
                .size(18.dp)
                .background(SubBlack, CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null, // 여기가 null이면 물결이 안 생깁니다.
                    onClick = onRemove
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "삭제",
                tint = White,
                // [수정] 아이콘 크기 12dp -> 10dp로 축소
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

@Composable
fun SeatNowMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = SubBlack, // 기본 검정
    showArrow: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp), // 터치 영역 및 간격 확보
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium), // 스타일 통일
            color = textColor
        )

        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = SubLightGray,
                modifier = Modifier.size(24.dp)
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽 타이틀
        Text(
            text = "실시간 좌석 관리",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
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
    val shape = RoundedCornerShape(50) // 완전 둥근 캡슐 모양

    // 1. 애니메이션 상태 정의
    // 이용 좌석(OCCUPIED)일 때 true
    val isOccupied = currentMode == SeatManagementViewModel.SeatDisplayMode.OCCUPIED

    // [핵심] 텍스트 색상 애니메이션 (부드럽게 전환)
    val emptyTextColor by animateColorAsState(
        targetValue = if (isOccupied) PointRed else Color.White,
        label = "EmptyText"
    )
    val occupiedTextColor by animateColorAsState(
        targetValue = if (isOccupied) Color.White else PointRed,
        label = "OccupiedText"
    )

    // [핵심] 배경 알약 이동 애니메이션 (Alignment를 이용해 좌우 슬라이딩)
    // Bias: -1(왼쪽), 1(오른쪽) -> 이를 부드럽게 전환
    val alignmentBias by animateFloatAsState(
        targetValue = if (isOccupied) 1f else -1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing), // 300ms 동안 쫀득하게 이동
        label = "Slide"
    )

    Box(
        modifier = Modifier
            .width(containerWidth)
            .height(containerHeight)
            .border(1.dp, PointRed, shape) // XML 디자인과 동일한 빨간 테두리
            .clip(shape)
            .background(Color.White)
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
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f) // 정확히 절반 크기
                .align(BiasAlignment(horizontalBias = alignmentBias, verticalBias = 0f)) // 애니메이션 적용된 정렬
                .background(PointRed, shape) // XML 색상 적용
        )

        // 3. 텍스트 레이어 (배경 위에 겹쳐짐)
        Row(modifier = Modifier.fillMaxSize()) {
            // 빈 좌석 텍스트 (왼쪽)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                Text(
                    text = "빈 좌석",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = emptyTextColor // 색상 애니메이션 적용
                )
            }

            // 이용 좌석 텍스트 (오른쪽)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.weight(1f).fillMaxHeight()
            ) {
                Text(
                    text = "이용 좌석",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
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
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                    .clickable { onSelect(category.id) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Medium),
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽: 라벨 (빈 좌석 수/전체 좌석 수)
        Text(
            text = labelText,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = SubBlack
        )

        // 오른쪽: 수치 및 태그 이미지
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${targetCount}/${totalSeats}석",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SubBlack
            )

            Spacer(modifier = Modifier.width(8.dp))

            // [수정됨] 기존 Box(Text)를 제거하고 Image 컴포넌트로 XML 리소스 렌더링
            Image(
                painter = painterResource(id = tagResId),
                contentDescription = contentDesc,
                modifier = Modifier.height(20.dp), // XML 원본 높이(18dp)와 유사하게 조정
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun TableViewItem(
    item: TableItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp), // 상하 여백 적절히
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 테이블 이름 (왼쪽)
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = SubBlack
        )

        // 개수 표시 (오른쪽, 예: "2개")
        Text(
            text = "${item.currentCount}개",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 테이블 이름
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = SubBlack
        )

        // Stepper Control
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Minus Button
            IconButton(
                onClick = onDecrement,
                modifier = Modifier.size(24.dp)
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
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, PointRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${item.currentCount}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PointRed
                )
            }

            // Plus Button
            IconButton(
                onClick = onIncrement,
                modifier = Modifier.size(24.dp)
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

// [1] 상단 검색바 (홈/검색 공통 사용)
@Composable
fun HomeSearchBar(
    activeHeadCount: Int?,
    onClearFilter: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = White
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            // 1. 왼쪽: 돋보기 아이콘 (항상 고정)
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "검색",
                tint = PointRed,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 2. 중앙: 칩 또는 힌트 텍스트 (weight 1f로 공간 차지)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (activeHeadCount != null) {
                    // [필터 상태] : 노란색 칩 표시 ("4명 자리") & 힌트 텍스트 숨김
                    Surface(
                        color = ColorSearchTag,
                        shape = RoundedCornerShape(8.dp), // 둥근 모서리
                        modifier = Modifier.height(30.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = "${activeHeadCount}명 자리",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = SubBlack
                            )
                        }
                    }
                } else {
                    // [기본 상태] : 힌트 텍스트 표시
                    Text(
//                        text = "장소, 지역, 대학명 검색",
                        text = "N명 자리찾기를 시작하세요!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubGray
                    )
                }
            }

            // 3. 오른쪽: X 버튼 (필터가 있을 때만 맨 오른쪽에 표시)
            if (activeHeadCount != null) {
                // 터치 영역 확보를 위해 Box 사용 권장
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(onClick = onClearFilter),
                    contentAlignment = Alignment.Center
                ) {
                    // 사진처럼 회색 동그라미 배경의 X 아이콘을 원하시면 수정 가능,
                    // 여기선 깔끔한 아이콘으로 구현
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "필터 삭제",
                        tint = SubGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// [2] 현 지도에서 검색 버튼
@Composable
fun SearchHereButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(50),
        color = White,
        shadowElevation = 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = PointRed,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "검색 중...",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = PointRed
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = PointRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "현 지도에서 검색",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = PointRed
                )
            }
        }
    }
}

// [3] 현재 위치 버튼
@Composable
fun CurrentLocationButton(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val iconColor = if (isSelected) PointRed else SubDarkGray

    Box(
        modifier = modifier
            .size(48.dp)
            .shadow(4.dp, CircleShape)
            .clip(CircleShape)
            .background(White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_current),
            contentDescription = "현재 위치",
            tint = iconColor
        )
    }
}

/**
 * [통합 지도 컴포넌트]
 * 지도 + 마커 + 상단 검색바 + 재검색 버튼 + 현재 위치 버튼이 모두 포함됨.
 * 홈 화면과 검색 화면에서 공통으로 사용.
 */
@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun UserMapContent(
    cameraPositionState: CameraPositionState,
    locationSource: LocationSource,
    storeList: List<Store>,
    trackingMode: LocationTrackingMode,
    isLoading: Boolean = false,
    onSearchHereClick: () -> Unit,
    onCurrentLocationClick: () -> Unit,
    onMapGestured: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. 네이버 지도 (Base)
        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            locationSource = locationSource,
            uiSettings = MapUiSettings(
                isLocationButtonEnabled = false,
                isZoomControlEnabled = false
            ),
            properties = MapProperties(
                locationTrackingMode = trackingMode
            )
        ) {
            storeList.forEachIndexed { index, store ->
                if (index < 10) {
                    val markerIcon = createMarkerBitmap(index + 1, store.status)

                    Marker(
                        state = MarkerState(position = LatLng(store.latitude, store.longitude)),
                        captionText = store.name,
                        captionOffset = 5.dp,
                        icon = OverlayImage.fromBitmap(markerIcon),
                        onClick = { true }
                    )
                }
            }

            MapEffect(Unit) { naverMap ->
                naverMap.addOnCameraChangeListener { reason, _ ->
                    if (reason == CameraUpdate.REASON_GESTURE) {
                        onMapGestured()
                    }
                }
            }

        }

        // 3. 현 지도에서 검색 버튼 (검색바 아래)
        SearchHereButton(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp),
            isLoading = isLoading,
            onClick = onSearchHereClick
        )

        // 4. 현재 위치 버튼 (우측 하단)
        CurrentLocationButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 32.dp),
            isSelected = trackingMode == LocationTrackingMode.Follow,
            onClick = onCurrentLocationClick
        )
    }
}