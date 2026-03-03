package com.gmg.seatnow.presentation.owner.store.storeManage.storeManageEdit

import android.net.Uri
import com.gmg.seatnow.domain.model.OperatingScheduleItem
import com.gmg.seatnow.domain.model.StoreMenuCategory
import com.gmg.seatnow.domain.model.StoreMenuItemData

data class StoreImageUiModel(
    val id: Long? = null,
    val uri: String,
    val isMain: Boolean = false,
    val isNew: Boolean = false
)

data class StoreEditUiState(
    val selectedTabIndex: Int = 0,
    val isSaveButtonEnabled: Boolean = false,
    
    val menuState: MenuEditState = MenuEditState(),
    val operationState: OperationInfoState = OperationInfoState(),
    val photoState: StorePhotoState = StorePhotoState(),
    val dialogState: DialogState = DialogState()
)

data class MenuEditState(
    val isCategoryEditMode: Boolean = false,
    val editingCategory: StoreMenuCategory? = null,
    val isAddingCategory: Boolean = false,
    val addingMenuCategoryId: Long? = null,
    val editingMenuItem: Pair<Long, StoreMenuItemData>? = null,
    val menuCategories: List<StoreMenuCategory> = emptyList()
)

data class OperationInfoState(
    val regularHolidayType: Int = 0, // 0:없음, 1:매주, 2:매월
    val weeklyHolidayDays: Set<Int> = emptySet(),
    val monthlyHolidayWeeks: Set<Int> = emptySet(),
    val monthlyHolidayDays: Set<Int> = emptySet(),
    val isTempHolidayEnabled: Boolean = false,
    val tempHolidayStart: String = "",
    val tempHolidayEnd: String = "",
    val operatingSchedules: List<OperatingScheduleItem> = emptyList()
)

data class StorePhotoState(
    val storePhotoList: List<StoreImageUiModel> = emptyList()
) {
    val representativePhotoUri: String?
        get() = storePhotoList.find { it.isMain }?.uri
}

data class DialogState(
    val showWeeklyDayDialog: Boolean = false,
    val showMonthlyWeekDialog: Boolean = false,
    val showMonthlyDayDialog: Boolean = false,
    val showTempHolidayDatePicker: Boolean = false
)

sealed interface StoreEditMainEvent {
    data object NavigateBack : StoreEditMainEvent
    data class ShowToast(val message: String) : StoreEditMainEvent
}

sealed interface StoreEditAction {
    data class OpenRenameDialog(val category: StoreMenuCategory) : StoreEditAction
    object DismissRenameDialog : StoreEditAction
    data class UpdateCategoryName(val categoryId: Long, val newName: String) : StoreEditAction
    object OpenAddCategoryDialog : StoreEditAction
    object DismissAddCategoryDialog : StoreEditAction
    data class ConfirmAddCategory(val name: String) : StoreEditAction

    data class SetCategoryEditMode(val isEdit: Boolean) : StoreEditAction
    data class ToggleRegularHolidayType(val type: Int) : StoreEditAction
    data class SetWeeklyDialogVisible(val visible: Boolean) : StoreEditAction
    data class SetMonthlyWeekDialogVisible(val visible: Boolean) : StoreEditAction
    data class SetMonthlyDayDialogVisible(val visible: Boolean) : StoreEditAction
    data class SetTempHolidayDatePickerVisible(val visible: Boolean) : StoreEditAction
    data class UpdateWeeklyHolidays(val days: Set<Int>) : StoreEditAction
    data class UpdateMonthlyWeeks(val weeks: Set<Int>) : StoreEditAction
    data class UpdateMonthlyDays(val days: Set<Int>) : StoreEditAction

    object ToggleTempHoliday : StoreEditAction
    data class UpdateTempHolidayRange(val start: String, val end: String) : StoreEditAction

    object AddOperatingSchedule : StoreEditAction
    data class UpdateOperatingDays(val id: Long, val dayIdx: Int) : StoreEditAction
    data class UpdateOperatingTime(val id: Long, val startHour: Int, val startMin: Int, val endHour: Int, val endMin: Int) : StoreEditAction
    data class RemoveOperatingSchedule(val id: Long) : StoreEditAction

    data class MoveMenuItem(val categoryId: Long, val fromIndex: Int, val toIndex: Int) : StoreEditAction

    data class MoveCategory(val fromIndex: Int, val toIndex: Int) : StoreEditAction
    data class DeleteCategory(val categoryId: Long) : StoreEditAction
    object AddCategory : StoreEditAction
    object SaveCategories : StoreEditAction

    data class OpenAddMenu(val categoryId: Long) : StoreEditAction
    object DismissAddMenu : StoreEditAction
    data class ConfirmAddMenu(
        val categoryId: Long,
        val name: String,
        val price: String,
        val imageUri: String?
    ) : StoreEditAction
    data class OpenEditMenu(val categoryId: Long, val item: StoreMenuItemData) : StoreEditAction
    object DismissEditMenu : StoreEditAction
    data class UpdateMenuItem(
        val originalCategoryId: Long,
        val newCategoryId: Long,
        val updatedItem: StoreMenuItemData
    ) : StoreEditAction
    data class DeleteMenuItem(val categoryId: Long, val itemId: Long) : StoreEditAction
    data class AddStorePhotos(val uris: List<Uri>) : StoreEditAction
    data class RemoveStorePhoto(val uriString: String) : StoreEditAction
    data class SetRepresentativePhoto(val uriString: String) : StoreEditAction
    object SaveStorePhotos : StoreEditAction
}
