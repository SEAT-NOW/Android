package com.gmg.seatnow.presentation.user.detail.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.presentation.user.detail.components.StoreMenuItem // ★ 분리된 아이템 임포트

@Composable
fun StoreMenuTab(
    menuCategories: List<MenuCategoryUiModel>,
    onLikeClicked: (Long) -> Unit,
    showLikeButton: Boolean = true
) {
    if (menuCategories.isEmpty() || menuCategories.all { it.menuItems.isEmpty() }) {
        Column(modifier = Modifier.fillMaxSize().padding(start = 24.dp, top = 24.dp)) {
            Text(text = "아직 메뉴 정보가 없어요", style = Body1_Medium_14, color = SubGray, fontWeight = FontWeight.Medium)
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            menuCategories.forEachIndexed { index, category ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), thickness = 1.dp, color = SubLightGray)
                }

                val topPadding = if (index == 0) 0.dp else 24.dp
                Text(
                    text = category.categoryName,
                    style = Body1_Medium_14,
                    color = SubGray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 24.dp, top = topPadding, bottom = 24.dp)
                )

                category.menuItems.forEach { item ->
                    // ★ 분리된 StoreMenuItem 호출
                    StoreMenuItem(
                        item = item,
                        onLikeClicked = { onLikeClicked(item.id) },
                        showLikeButton = showLikeButton
                    )
                }
            }
        }
    }
}