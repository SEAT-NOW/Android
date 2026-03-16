package com.gmg.seatnow.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.theme.SubBlack
import com.gmg.seatnow.presentation.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatNowTopAppBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier.Companion,
    topMargin: Dp = 0.dp
) {
    Column(modifier = modifier.padding(top = topMargin)) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Companion.Bold),
                    modifier = Modifier.Companion.offset(x = (-6).dp)
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "뒤로가기",
                        modifier = Modifier.Companion.size(32.dp)
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