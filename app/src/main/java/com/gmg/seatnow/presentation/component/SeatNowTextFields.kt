package com.gmg.seatnow.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import com.gmg.seatnow.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmg.seatnow.presentation.extension.bottomShadow
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SubBlack
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.SubLightGray
import com.gmg.seatnow.presentation.theme.White

class SeatNowTextFields {
}

// ★ 병합된 통합 텍스트 필드
@Composable
fun SeatNowTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier.Companion,
    height : Dp = 52.dp,
    isEnabled: Boolean = true,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.Companion.None,
    isPassword: Boolean = false,
    errorText: String? = null,
    imeAction: ImeAction = ImeAction.Companion.Next,
    keyboardType: KeyboardType = KeyboardType.Companion.Text
) {
    val backgroundColor = if (isEnabled) White else SubLightGray
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val finalKeyboardType = if (isPassword) KeyboardType.Companion.Password else keyboardType

    val borderColor = if (errorText != null) Color.Companion.Red
    else if (isFocused) SubBlack
    else SubLightGray

    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            interactionSource = interactionSource,
            modifier = Modifier.Companion
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
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
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
            trailingIcon = {
                if (isPassword) {
                    val image = if (passwordVisible)
                        painterResource(id = R.drawable.btn_pw_visible_on)
                    else
                        painterResource(id = R.drawable.btn_pw_visible_off)

                    // 컴포넌트 여백 요구사항(오른쪽 16dp, 동일 패딩)에 맞추기 위해 Box나 패딩 조절 가능
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.padding(end = 4.dp) // OutlinedTextField 기본 패딩이 있어서 추가로 살짝만 줘도 보통 맞습니다.
                    ) {
                        Icon(painter = image, contentDescription = "Toggle password visibility", tint = Color.Companion.Unspecified)
                    }
                }
            },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            singleLine = true,
            isError = errorText != null,
            readOnly = readOnly,
            enabled = isEnabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Companion.Transparent,
                unfocusedContainerColor = Color.Companion.Transparent,
                disabledContainerColor = Color.Companion.Transparent,
                errorContainerColor = Color.Companion.Transparent,
                focusedTextColor = SubBlack,
                unfocusedTextColor = SubBlack,
                focusedBorderColor = Color.Companion.Transparent,
                unfocusedBorderColor = Color.Companion.Transparent,
                disabledBorderColor = Color.Companion.Transparent,
                errorBorderColor = Color.Companion.Transparent
            ),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else visualTransformation,
            keyboardOptions = KeyboardOptions(
                keyboardType = finalKeyboardType,
                imeAction = imeAction
            )
        )
        if (errorText != null) {
            Spacer(modifier = Modifier.Companion.height(4.dp))
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Companion.Red),
                modifier = Modifier.Companion.padding(start = 4.dp)
            )
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
    modifier: Modifier = Modifier.Companion,
    height: Dp = 52.dp,
    isEnabled: Boolean = true,
    isButtonEnabled: Boolean = true,
    timerText: String? = null,
    errorText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Companion.Text,
    visualTransformation: VisualTransformation = VisualTransformation.Companion.None,
    onButtonClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = if (errorText != null) Color.Companion.Red else if (isFocused) SubBlack else SubLightGray

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.Companion
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
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )
                .background(White, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                .padding(end = 8.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = isEnabled,
                modifier = Modifier.Companion.weight(1f),
                interactionSource = interactionSource,
                placeholder = {
                    if (!isFocused) {
                        Text(
                            text = placeholder,
                            color = SubLightGray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = visualTransformation,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Companion.Transparent,
                    focusedContainerColor = Color.Companion.Transparent,
                    disabledContainerColor = Color.Companion.Transparent,
                    unfocusedBorderColor = Color.Companion.Transparent,
                    focusedBorderColor = Color.Companion.Transparent,
                    disabledBorderColor = Color.Companion.Transparent,
                    errorContainerColor = Color.Companion.Transparent,
                    errorBorderColor = Color.Companion.Transparent,
                    disabledTextColor = SubGray
                ),
                singleLine = true,
                isError = errorText != null
            )
            if (timerText != null) {
                Text(
                    text = timerText,
                    color = SubGray,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.Companion.padding(end = 8.dp)
                )
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
                modifier = Modifier.Companion.height(28.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Companion.Bold)
                )
            }
        }
        if (errorText != null) {
            Spacer(modifier = Modifier.Companion.height(4.dp))
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Companion.Red),
                modifier = Modifier.Companion.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun CircularNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier.Companion,
    isEnabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val mainColor = if(isEnabled) PointRed else SubGray

    Box(
        modifier = modifier
            .size(48.dp)
            .border(2.dp, mainColor, CircleShape)
            .background(Color.Companion.Transparent, CircleShape),
        contentAlignment = Alignment.Companion.Center
    ) {
        if (value.isEmpty() && !isFocused) {
            Text(
                text = placeholder,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    color = mainColor,
                    textAlign = TextAlign.Companion.Center
                )
            )
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
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Companion.Bold,
                color = SubBlack,
                textAlign = TextAlign.Companion.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Number),
            singleLine = true,
            cursorBrush = SolidColor(mainColor),
            modifier = Modifier.Companion.wrapContentSize()
        )
    }
}