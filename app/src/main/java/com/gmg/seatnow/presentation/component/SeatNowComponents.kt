package com.gmg.seatnow.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
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
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = modifier
            .size(48.dp) // 원 크기 고정
            .border(2.dp, PointRed, CircleShape) // 빨간 테두리
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
                    color = PointRed,
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
            // InteractionSource 연결
            interactionSource = interactionSource,
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SubBlack,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            // ★ 커서 색상 다시 빨간색으로 복구 (보이게)
            cursorBrush = SolidColor(PointRed),
            modifier = Modifier.wrapContentSize()
        )
    }
}

// ★ [공통 컴포넌트] 빨간색 플러스 버튼
@Composable
fun SeatNowRedPlusButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(100.dp)
            .height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PointRed,
            contentColor = White
        ),
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
    onDeleteClick: () -> Unit
) {
    val contentColor = if (isSelected) PointRed else SubDarkGray
    val borderColor = if (isSelected) PointRed else SubLightGray

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
                .border(1.dp, borderColor, RoundedCornerShape(4.dp)) // 각진 테두리 (이미지 참고)
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

        // 2. 수정 아이콘 (진한 회색 연필)
        IconButton(onClick = onEditClick, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "수정",
                tint = SubDarkGray
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 3. 삭제 아이콘 (빨간 휴지통)
        IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "삭제",
                tint = PointRed
            )
        }
    }
}

@Composable
fun TableItemCard(
    item: TableItem,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // N (원형)
        Box(
            modifier = Modifier
                .size(40.dp) // 크기 약간 조정
                .border(1.dp, PointRed, CircleShape)
                .background(White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.personCount,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = PointRed
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "인 테이블", style = MaterialTheme.typography.bodyMedium, color = SubBlack)
        Spacer(modifier = Modifier.width(16.dp))

        // X 아이콘 (굵은 X)
        Icon(
            painter = painterResource(id = R.drawable.ic_table_multiply), // 커스텀 아이콘 있으면 사용
            contentDescription = "multiply",
            tint = PointRed,
            modifier = Modifier.size(12.dp) // 굵게 보이려면 stroke 조절 필요하나 일단 기본 사용
        )

        Spacer(modifier = Modifier.width(16.dp))

        // M (원형)
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(1.dp, PointRed, CircleShape)
                .background(White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.tableCount,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = PointRed
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "개", style = MaterialTheme.typography.bodyMedium, color = SubBlack)

        Spacer(modifier = Modifier.width(24.dp))

        // 삭제 버튼
        IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "삭제",
                tint = PointRed
            )
        }
    }
}