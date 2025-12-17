package com.gmg.seatnow.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
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
    isPassword: Boolean = false,
    errorText: String? = null,           // null이면 에러 없음 (Simple 모드)
    imeAction: ImeAction = ImeAction.Next,
    keyboardType: KeyboardType = KeyboardType.Text // 기본값 Text, 필요시 Number/Email 등 설정 가능
) {
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
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = White,
                focusedContainerColor = White,
                unfocusedBorderColor = Color.Transparent, // border는 modifier로 처리
                focusedBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent,
                errorContainerColor = White
            ),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
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

// ★ 수정됨: 버튼이 포함된 텍스트 필드 (에러 로직 적용)
@Composable
fun SignUpTextFieldWithButton(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    buttonText: String,
    modifier: Modifier = Modifier,
    height : Dp = 52.dp,
    buttonColor: Color = SubLightGray,
    buttonTextColor: Color = SubDarkGray,
    timerText: String? = null,
    errorText: String? = null,           // 에러 메시지 파라미터 추가
    keyboardType: KeyboardType = KeyboardType.Text, // 키보드 타입 추가
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
                modifier = Modifier.weight(1f),
                interactionSource = interactionSource, // 포커스 소스 연결
                placeholder = {
                    Text(text = placeholder, color = SubLightGray, style = MaterialTheme.typography.bodyMedium)
                },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    errorBorderColor = Color.Transparent
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
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = buttonText,
                    color = buttonTextColor,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
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
fun TermItem(title: String, showArrow: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = SubGray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = SubDarkGray)
        }

        if (showArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "상세보기",
                tint = SubDarkGray
            )
        }
    }
}