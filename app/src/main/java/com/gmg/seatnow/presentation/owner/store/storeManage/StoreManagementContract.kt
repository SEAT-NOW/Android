package com.gmg.seatnow.presentation.owner.store.storeManage

import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.StoreDetail

data class StoreManagementUiState(
    val loadState: LoadState = LoadState(),
    val storeData: StoreDataState = StoreDataState()
) {
    data class LoadState(
        val isLoading: Boolean = false
    )
    
    data class StoreDataState(
        val storeDetail: StoreDetail? = null,
        val menuCategories: List<MenuCategoryUiModel> = emptyList()
    )
}

sealed interface StoreManagementAction {
    data object ReloadData : StoreManagementAction
}

sealed interface StoreManagementEvent {
    data class ShowToast(val message: String) : StoreManagementEvent
}
