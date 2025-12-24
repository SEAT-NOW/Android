package com.gmg.seatnow.presentation.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.extension.bottomShadow
import com.gmg.seatnow.presentation.theme.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.gmg.seatnow.presentation.owner.dataClass.TableItem
import com.gmg.seatnow.R

// ★ 병합된 통합 텍스트 필드
@Composable
fun SeatNowTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height : Dp = 52.dp,
    isEnabled: Boolean = true,         // TextField 입력 가능 여부
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isPassword: Boolean = false,
    errorText: String? = null,           // null이면 에러 없음 (Simple 모드)
    imeAction: ImeAction = ImeAction.Next,
    keyboardType: KeyboardType = KeyboardType.Text // 기본값 Text, 필요시 Number/Email 등 설정 가능
) {
    val backgroundColor = if (isEnabled) White else SubLightGray

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // 비밀번호면 무조건 Password 타입, 아니면 전달받은 keyboardType 사용
    val finalKeyboardType = if (isPassword) KeyboardType.Password else keyboardType

    // 테두리 색상 로직 (에러 > 포커스 > 기본)
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
                .bottomShadow(
                    offsetY = 2.dp,
                    shadowBlurRadius = 4.dp,
                    alpha = 0.15f,
                    cornersRadius = 12.dp
                )
                .background(color = backgroundColor, shape = RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp)
                ),
            placeholder = {
                if (!isFocused) {
                    Text(
                        text = placeholder,
                        color = SubLightGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
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
                disabledContainerColor = Color.Transparent, // 이게 이미지 버그의 원인이었습니다.
                errorContainerColor = Color.Transparent,

                // 텍스트/커서 색상
                focusedTextColor = SubBlack,
                unfocusedTextColor = SubBlack,

                // 테두리도 Modifier로 그렸으니 여기선 투명
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent
            ),
                visualTransformation = if (isPassword) PasswordVisualTransformation() else visualTransformation,

            keyboardOptions = KeyboardOptions(
                keyboardType = finalKeyboardType,
                imeAction = imeAction
            )
        )

        // 에러 텍스트가 있을 때만 표시
        if (errorText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Red),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

// 버튼이 포함된 텍스트 필드 (에러 로직 적용)
@Composable
fun SignUpTextFieldWithButton(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    buttonText: String,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp, // 기본 높이

    // 상태 제어
    isEnabled: Boolean = true,         // TextField 입력 가능 여부
    isButtonEnabled: Boolean = true,   // 버튼 클릭 가능 여부

    timerText: String? = null,
    errorText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onButtonClick: () -> Unit
) {
    // 1. 포커스 감지 상태 추가
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // 2. 테두리 색상 로직 (에러 > 포커스 > 기본)
    val borderColor = if (errorText != null) Color.Red
    else if (isFocused) SubBlack
    else SubLightGray

    // 3. Column으로 감싸서 하단에 에러 메시지 배치 공간 확보
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .bottomShadow(
                    offsetY = 2.dp,
                    shadowBlurRadius = 4.dp,
                    alpha = 0.15f,
                    cornersRadius = 12.dp
                )
                .border(
                    width = 1.dp,
                    color = borderColor, // 동적 컬러 적용
                    shape = RoundedCornerShape(12.dp)
                )
                .background(White, RoundedCornerShape(12.dp))
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = isEnabled,
                modifier = Modifier.weight(1f),
                interactionSource = interactionSource, // 포커스 소스 연결
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
                    disabledContainerColor = Color.Transparent, // 비활성화돼도 배경 투명(부모 Row가 배경색 담당)
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                    disabledTextColor = SubGray // 비활성화 시 텍스트 색상
                ),
                singleLine = true,
                isError = errorText != null
            )

            if (timerText != null) {
                Text(
                    text = timerText,
                    color = SubGray,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Button(
                onClick = onButtonClick,
                enabled = isButtonEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PointRed, // 활성화 시 색상
                    contentColor = White,
                    disabledContainerColor = SubLightGray, // 비활성화 시 색상
                    disabledContentColor = White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        // 4. 에러 메시지 표시 영역
        if (errorText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Red),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun TermItem(
    title: String,
    isChecked: Boolean, // 체크 상태 받음
    showArrow: Boolean,
    onToggle: () -> Unit, // 체크박스(행) 클릭 시
    onDetailClick: () -> Unit = {} // 화살표 클릭 시
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle) // 행 전체 클릭 시 체크 토글
            .padding(vertical = 6.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                // ★ 체크되면 PointRed, 아니면 SubLightGray
                tint = if (isChecked) PointRed else SubLightGray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = Body2_Regular_14,
                // ★ 체크 여부와 상관없이 글자는 잘 보여야 하므로 SubDarkGray 유지 (요청 시 변경 가능)
                color = SubDarkGray
            )
        }

        if (showArrow) {
            IconButton(
                onClick = onDetailClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "상세보기",
                    tint = SubDarkGray
                )
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
    topMargin: Dp = 0.dp // ★ 상단 여백 조절 파라미터 추가
) {
    // TopAppBar는 내부적으로 높이가 정해져 있으므로,
    // 여백을 주고 싶다면 Column이나 Box로 감싸거나 Modifier.padding을 써야 합니다.
    Column(modifier = modifier.padding(top = topMargin)) { // ★ 상단 여백 적용
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.offset(x = (-6).dp) // 타이틀 미세 위치 조정 유지
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "뒤로가기",
                        modifier = Modifier.size(32.dp)
                    )
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

// ★ [공통 컴포넌트] 원형 숫자 입력 필드 (N, M 등 입력용)
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
            .size(48.dp) // 원 크기 고정
            .border(2.dp, mainColor, CircleShape) // 빨간 테두리
            .background(Color.Transparent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // 값이 비어있으면 Placeholder 표시
        if (value.isEmpty() && !isFocused) {
            Text(
                text = placeholder,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = mainColor,
                    textAlign = TextAlign.Center
                )
            )
        }
        // 실제 입력 필드
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
            cursorBrush = SolidColor(mainColor), // 커서 색상도 변경
            modifier = Modifier.wrapContentSize()
        )
    }
}

// ★ [공통 컴포넌트] 빨간색 플러스 버튼
@Composable
fun SeatNowRedPlusButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(100.dp)
            .height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if(isEnabled) PointRed else SubGray,
            contentColor = White
        ),
        enabled = isEnabled,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "추가",
            modifier = Modifier.size(24.dp)
        )
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
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onItemClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 빨간 테두리 박스 (이름 + 좌석수)
        Row(
            modifier = Modifier
                .weight(1f) // 남은 공간 차지
                .fillMaxHeight()
                .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                .background(White, RoundedCornerShape(4.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
            Text(
                text = "${seatCount}석",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 2. 수정 아이콘
        IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "수정",
                tint = SubDarkGray
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 3. 삭제 아이콘 (활성/비활성 처리)
        IconButton(
            onClick = onDeleteClick,
            enabled = isDeleteEnabled, // ★ 클릭 제한
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "삭제",
                tint = deleteIconColor // ★ 색상 적용
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
    isDeleteEnabled: Boolean = true, // 삭제 버튼 활성/비활성 제어
    isEnabled: Boolean = true
) {
    val iconColor = if (isEnabled) PointRed else SubGray
    val textColor = if (isEnabled) SubBlack else SubGray

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularNumberField(
            value = nValue,
            onValueChange = onNChange,
            placeholder = "N",
            isEnabled = isEnabled // 전달
        )

        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "인 테이블", style = MaterialTheme.typography.bodyMedium, color = if(isEnabled) SubBlack else SubGray)
        Spacer(modifier = Modifier.width(16.dp))

        Icon(
            painter = painterResource(id = R.drawable.ic_table_multiply),
            contentDescription = "multiply",
            tint = iconColor, // 아이콘 색상 변경
            modifier = Modifier.size(12.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        CircularNumberField(
            value = mValue,
            onValueChange = onMChange,
            placeholder = "M",
            isEnabled = isEnabled // 전달
        )

        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "개", style = MaterialTheme.typography.bodyMedium, color = if(isEnabled) SubBlack else SubGray)

        Spacer(modifier = Modifier.width(24.dp))

        IconButton(
            onClick = onDeleteClick,
            enabled = isDeleteEnabled && isEnabled, // 삭제 조건 추가
            modifier = Modifier.size(24.dp)
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
fun SeatNowDropdownButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .width(width)
            .height(24.dp)
            .border(1.dp, SubGray, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(start = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = SubGray,
                maxLines = 1
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = SubGray,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

// ★ [신규] 날짜 선택 박스 (이미지의 2025/12/23 박스)
@Composable
fun SeatNowDateBox(
    dateText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(110.dp)
            .height(24.dp)
            .border(1.dp, PointRed, RoundedCornerShape(4.dp)) // 선택된 느낌의 붉은 테두리
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium.copy(color = PointRed),
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = PointRed,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

// ★ [신규] 요일 선택기 (이미지의 빨간/흰색 원형 버튼)
@Composable
fun DayOfWeekSelector(
    selectedDays: Set<Int>, // 0:일, 1:월 ...
    disabledDays: Set<Int> = emptySet(), // 비활성화(정기휴무)된 요일
    onDayClick: (Int) -> Unit
) {
    val days = listOf("일", "월", "화", "수", "목", "금", "토")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        days.forEachIndexed { index, dayName ->
            val isSelected = selectedDays.contains(index)
            val isDisabled = disabledDays.contains(index)

            // 스타일 로직:
            // 1. 비활성 -> 회색 배경
            // 2. 선택됨 -> 빨간 배경, 흰 글씨
            // 3. 선택안됨 -> 흰 배경, 빨간 테두리, 빨간 글씨
            val backgroundColor = if (isDisabled) SubLightGray else if (isSelected) PointRed else White
            val contentColor = if (isDisabled) White else if (isSelected) White else PointRed
            val borderColor = if (isDisabled || isSelected) Color.Transparent else PointRed

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .border(1.dp, borderColor, CircleShape)
                    .clickable(enabled = !isDisabled) { onDayClick(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = contentColor
                )
            }
        }
    }
}

// ★ [신규] iOS 스타일 휠 피커 (Wheel Picker)
// 00, 01, 02... 처럼 가운데가 크고 위아래가 흐린 스크롤
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SeatNowWheelPicker(
    items: List<String>,
    initialIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    visibleItemsCount: Int = 3,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState(initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val itemHeight = 35.dp // 아이템 높이 설정

    // 스크롤 멈춤 감지 및 선택 업데이트
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex = listState.firstVisibleItemIndex + visibleItemsCount / 2
            val validIndex = centerIndex.coerceIn(0, items.lastIndex)
            onSelectionChanged(validIndex)
            listState.animateScrollToItem(validIndex - visibleItemsCount / 2)
        }
    }

    Box(
        modifier = modifier.height(itemHeight * visibleItemsCount),
        contentAlignment = Alignment.Center
    ) {
        // 선택 라인 (위/아래 구분선) - 디자인에 따라 추가/제거 가능
        // 이미지에는 구분선이 뚜렷하게 보이진 않고 폰트 크기/색상으로 구분됨

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight * (visibleItemsCount / 2)),
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items.size) { index ->
                // 현재 아이템이 가운데에 있는지 계산
                val isSelected = !listState.isScrollInProgress &&
                        (listState.firstVisibleItemIndex + visibleItemsCount / 2) == index

                Box(
                    modifier = Modifier.height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index],
                        // 선택되면 크고 진하게(Black), 아니면 작고 흐리게(Gray)
                        style = if (isSelected)
                            MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        else
                            MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                        color = if (isSelected) SubBlack else SubGray.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
@Composable
fun OperatingScheduleItem(
    schedule: com.gmg.seatnow.presentation.owner.dataClass.OperatingScheduleItem, // 데이터 클래스 경로 확인 필요
    isDeleteEnabled: Boolean,
    onUpdateStart: (Int, Int) -> Unit, // (hour, min)
    onUpdateEnd: (Int, Int) -> Unit,   // (hour, min)
    onDelete: () -> Unit
) {
    // 0~24시, 0~55분 데이터 리스트
    val hours = remember { (0..24).map { "%02d".format(it) } }
    val minutes = remember { (0..55 step 5).map { "%02d".format(it) } }

    // 현재 어떤 시간을 수정 중인지 상태 관리 (None, Start, End)
    var editingTarget by remember { mutableStateOf<TimeTarget>(TimeTarget.None) }

    val iconSize = 24.dp        // 휴지통 아이콘 크기
    val iconSpacing = 12.dp     // 시간과 휴지통 사이 간격
    val sideBalanceWidth = iconSize + iconSpacing // 좌측에 줄 투명 Spacer 너비

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. 상단: 시간 표시 텍스트 및 삭제 버튼 ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            // ★ 핵심: Row의 내용물 전체를 가운데 정렬
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // [1] 좌측 밸런스용 투명 Spacer (오른쪽 휴지통 영역만큼 공간 차지)
            Spacer(modifier = Modifier.width(sideBalanceWidth))

            // [2] 실제 시간 컨텐츠 (이 덩어리가 화면 정중앙에 오게 됨)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TimeDisplayBox(
                    hour = schedule.startHour,
                    minute = schedule.startMin,
                    isSelected = editingTarget == TimeTarget.Start,
                    onClick = {
                        editingTarget = if (editingTarget == TimeTarget.Start) TimeTarget.None else TimeTarget.Start
                    }
                )

                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "~",
                    style = MaterialTheme.typography.titleMedium,
                    color = SubGray
                )
                Spacer(modifier = Modifier.width(16.dp))

                TimeDisplayBox(
                    hour = schedule.endHour,
                    minute = schedule.endMin,
                    isSelected = editingTarget == TimeTarget.End,
                    onClick = {
                        editingTarget = if (editingTarget == TimeTarget.End) TimeTarget.None else TimeTarget.End
                    }
                )
            }

            // [3] 우측 휴지통 영역
            Spacer(modifier = Modifier.width(iconSpacing)) // 간격

            IconButton(
                onClick = onDelete,
                enabled = isDeleteEnabled,
                modifier = Modifier.size(iconSize) // 아이콘 크기
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = if (isDeleteEnabled) PointRed else SubLightGray
                )
            }
        }

        // --- 2. 하단: Wheel Picker (확장되었을 때만 보임) ---
        // 애니메이션 없이 즉시 전환 (요구사항 사진과 동일한 UX)
        if (editingTarget != TimeTarget.None) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 현재 편집 대상에 따라 초기값과 업데이트 함수 결정
                val initialHour = if (editingTarget == TimeTarget.Start) schedule.startHour else schedule.endHour
                val initialMinIndex = (if (editingTarget == TimeTarget.Start) schedule.startMin else schedule.endMin) / 5

                // 시간(Hour) Picker
                SeatNowWheelPicker(
                    items = hours,
                    initialIndex = initialHour,
                    onSelectionChanged = { idx ->
                        if (editingTarget == TimeTarget.Start) {
                            onUpdateStart(idx, schedule.startMin)
                        } else {
                            onUpdateEnd(idx, schedule.endMin)
                        }
                    },
                    modifier = Modifier.width(60.dp) // 넓이 살짝 여유있게
                )

                // 분(Minute) Picker
                SeatNowWheelPicker(
                    items = minutes,
                    initialIndex = initialMinIndex,
                    onSelectionChanged = { idx ->
                        val min = idx * 5
                        if (editingTarget == TimeTarget.Start) {
                            onUpdateStart(schedule.startHour, min)
                        } else {
                            onUpdateEnd(schedule.endHour, min)
                        }
                    },
                    modifier = Modifier.width(60.dp)
                )
            }
        }
    }
}

// [보조 컴포넌트] 시간 텍스트 (밑줄 포함)
@Composable
fun TimeDisplayBox(
    hour: Int,
    minute: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp) // 텍스트 영역 고정 너비
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "%02d:%02d".format(hour, minute),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = SubBlack
            )
            // 선택되었을 때만 ▲ 아이콘 표시 (사진 1 참고)
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown, // DropUp이 없으면 회전시킴
                    contentDescription = null,
                    modifier = Modifier.rotate(180f).size(20.dp),
                    tint = SubBlack
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        // 밑줄 (선택되면 검정색 굵게, 아니면 회색 얇게)
        HorizontalDivider(
            thickness = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) SubBlack else SubLightGray
        )
    }
}

// 상태 관리를 위한 Enum
enum class TimeTarget {
    None, Start, End
}
