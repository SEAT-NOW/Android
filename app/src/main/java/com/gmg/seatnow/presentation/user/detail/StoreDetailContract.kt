package com.gmg.seatnow.presentation.user.detail

import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.StoreDetail

data class StoreDetailUiState(
    val storeDetail: StoreDetail? = null,
    val menuCategories: List<MenuCategoryUiModel> = emptyList(),
    val isLoading: Boolean = true
)

sealed interface StoreDetailAction {
    data class OnKeepClicked(val storeId: Long, val newKeptState: Boolean) : StoreDetailAction
    data class OnLikeClicked(val menuId: Long) : StoreDetailAction
    object OnBackClick : StoreDetailAction
}

sealed interface StoreDetailEvent {
    data class ShowToast(val message: String) : StoreDetailEvent
    object NavigateBack : StoreDetailEvent
}
