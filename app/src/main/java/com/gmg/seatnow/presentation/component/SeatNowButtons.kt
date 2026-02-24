package com.gmg.seatnow.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.SubLightGray
import com.gmg.seatnow.presentation.theme.SubPaleGray
import com.gmg.seatnow.presentation.theme.White

@Composable
fun SeatNowRedPlusButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier.Companion,
    isEnabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.width(100.dp).height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEnabled) PointRed else SubGray,
            contentColor = White
        ),
        enabled = isEnabled,
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "추가",
            modifier = Modifier.Companion.size(24.dp)
        )
    }
}

@Composable
fun SeatNowDropdownButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.Companion,
    borderColor: Color = SubGray,
    textColor: Color = SubGray,
    enabled: Boolean = true
) {

    Box(
        modifier = modifier
            .height(24.dp)
            .border(1.dp, borderColor, androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
            .then(
                // enabled가 true일 때만 clickable 적용
                if (enabled) Modifier.Companion.clickable(onClick = onClick) else Modifier.Companion
            )
            .padding(start = 8.dp),
        contentAlignment = Alignment.Companion.Center
    ) {
        Row(
            verticalAlignment = Alignment.Companion.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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

@Composable
fun SeatNowCheckRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.Companion,
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
        contentAlignment = Alignment.Companion.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = White,
                modifier = Modifier.Companion.size(size * 0.7f)
            )
        }
    }
}

@Composable
fun AddPhotoButton(
    onClick: () -> Unit,
    currentCount: Int,
    maxCount: Int = 5,
    modifier: Modifier = Modifier.Companion
) {
    // [수정] X버튼 공간 확보용 패딩을 10dp -> 6dp로 축소
    Box(modifier = modifier.padding(top = 6.dp, end = 6.dp)) {
        Box(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .aspectRatio(4f / 5f) // 4:5 비율
                .background(SubPaleGray, RectangleShape)
                .clickable(onClick = onClick)
                .border(1.dp, SubLightGray, RectangleShape),
            contentAlignment = Alignment.Companion.Center
        ) {
            Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "사진 추가",
                    tint = SubGray,
                    modifier = Modifier.Companion.size(32.dp)
                )
                Spacer(modifier = Modifier.Companion.height(4.dp))
                Text(
                    text = "$currentCount / $maxCount",
                    style = MaterialTheme.typography.labelMedium,
                    color = SubGray
                )
            }
        }
    }
}