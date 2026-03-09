package com.gmg.seatnow.domain.usecase.owner.store

import com.gmg.seatnow.domain.model.StoreMenuCategory
import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.MenuItemUiModel
import javax.inject.Inject

import com.gmg.seatnow.domain.model.StoreMenuItemData

class FormatMenuCategoryUseCase @Inject constructor() {
    operator fun invoke(domainCategories: List<StoreMenuCategory>): List<MenuCategoryUiModel> {
        return domainCategories.map { category ->
            MenuCategoryUiModel(
                categoryName = category.name,
                menuItems = category.items.map { item: StoreMenuItemData ->
                    MenuItemUiModel(
                        id = item.id,
                        name = item.name,
                        // "22,000" 문자열에서 쉼표 제거 후 Int 변환
                        price = item.price.replace(",", "").toIntOrNull() ?: 0,
                        imageUrl = item.imageUrl ?: "",
                        isRecommended = false,
                        isLiked = false
                    )
                }
            )
        }
    }
}
