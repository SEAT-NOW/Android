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
                Text(
                    text = placeholder,
                    color = SubLightGray,
                    style = MaterialTheme.typography.bodyMedium
                )
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
                    Text(text = placeholder, color = SubLightGray, style = MaterialTheme.typography.bodyMedium)
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