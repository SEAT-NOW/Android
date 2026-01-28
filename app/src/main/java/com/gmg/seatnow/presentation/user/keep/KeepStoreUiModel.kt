package com.gmg.seatnow.presentation.user.keep

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.presentation.theme.*

// 킵 화면 전용 UI 모델 (DTO 역할)
data class KeepStoreUiModel(
    val storeId: Long,
    val storeName: String,
    val imageUrl: String, // 실제로는 URL이겠지만 여기선 더미 로직 처리
    val status: StoreStatus,
    val universityName: String,
    val availableSeats: Int,
    val totalSeats: Int,
    val isKept: Boolean = true
)