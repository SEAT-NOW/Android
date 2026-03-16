package com.gmg.seatnow.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.theme.Body2_Regular_14
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.SubDarkGray
import com.gmg.seatnow.presentation.theme.SubLightGray

@Composable
fun TermItem(
    title: String,
    isChecked: Boolean,
    showArrow: Boolean,
    onToggle: () -> Unit,
    onDetailClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 6.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.Companion.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.Companion.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (isChecked) PointRed else SubLightGray,
                modifier = Modifier.Companion.size(20.dp)
            )
            Spacer(modifier = Modifier.Companion.width(8.dp))
            Text(text = title, style = Body2_Regular_14, color = SubDarkGray)
        }
        if (showArrow) {
            IconButton(onClick = onDetailClick, modifier = Modifier.Companion.size(24.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "상세보기",
                    tint = SubDarkGray
                )
            }
        }
    }
}