package com.gmg.seatnow.presentation.owner.store.storeManage.storeManageEdit

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.OperatingScheduleItem
import com.gmg.seatnow.domain.model.OpeningHour
import com.gmg.seatnow.domain.model.RegularHoliday
import com.gmg.seatnow.domain.model.StoreMenuCategory
import com.gmg.seatnow.domain.model.StoreMenuItemData
import com.gmg.seatnow.domain.model.TemporaryHoliday
import com.gmg.seatnow.domain.usecase.owner.store.DeleteMenuUseCase
import com.gmg.seatnow.domain.usecase.owner.store.GetStoreImagesUseCase
import com.gmg.seatnow.domain.usecase.user.detail.GetStoreMenusUseCase
import com.gmg.seatnow.domain.usecase.owner.store.GetStoreOperationInfoUseCase
import com.gmg.seatnow.domain.usecase.owner.store.SaveMenuUseCase
import com.gmg.seatnow.domain.usecase.owner.store.UpdateMenuCategoriesUseCase
import com.gmg.seatnow.domain.usecase.owner.store.UpdateMenuOrdersUseCase
import com.gmg.seatnow.domain.usecase.owner.store.UpdateStoreImagesUseCase
import com.gmg.seatnow.domain.usecase.owner.store.UpdateStoreOperationInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class StoreEditMainViewModel @Inject constructor(
    private val getStoreOperationInfoUseCase: GetStoreOperationInfoUseCase,
    private val updateStoreOperationInfoUseCase: UpdateStoreOperationInfoUseCase,
    private val updateMenuCategoriesUseCase: UpdateMenuCategoriesUseCase,
    private val getStoreMenusUseCase: GetStoreMenusUseCase,
    private val saveMenuUseCase: SaveMenuUseCase,
    private val getStoreImagesUseCase: GetStoreImagesUseCase,
    private val updateStoreImagesUseCase: UpdateStoreImagesUseCase,
    private val updateMenuOrdersUseCase: UpdateMenuOrdersUseCase,
    private val deleteMenuUseCase: DeleteMenuUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreEditUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<StoreEditMainEvent>()
    val event = _event.asSharedFlow()

    init {
        loadOperationInfo()
        loadStoreMenuData()
        fetchStorePhotos()
    }

    private fun loadStoreMenuData() {
        viewModelScope.launch {
            getStoreMenusUseCase(forceRefresh = true)
                .onSuccess { menuCategories ->
                    if (menuCategories.isEmpty()) {
                        initializeDefaultMenuData()
                    } else {
                        _uiState.update { it.copy(menuState = it.menuState.copy(menuCategories = menuCategories)) }
                    }
                    checkSaveButtonEnabled()
                }
                .onFailure {
                    initializeDefaultMenuData()
                }
        }
    }

    private fun initializeDefaultMenuData() {
        val currentCategories = _uiState.value.menuState.menuCategories
        if (currentCategories.isEmpty()) {
            val defaultCategories = listOf(
                StoreMenuCategory(id = -1, name = "메인메뉴", items = emptyList()),
                StoreMenuCategory(id = -2, name = "사이드메뉴", items = emptyList()),
                StoreMenuCategory(id = -3, name = "주류", items = emptyList())
            )
            _uiState.update { it.copy(menuState = it.menuState.copy(menuCategories = defaultCategories)) }
        }
    }

    private fun loadOperationInfo() {
        viewModelScope.launch {
            getStoreOperationInfoUseCase()
                .onSuccess { info ->
                    _uiState.update { currentState ->
                        var type = 0
                        val weeklyDays = mutableSetOf<Int>()
                        val monthlyWeeks = mutableSetOf<Int>()
                        val monthlyDays = mutableSetOf<Int>()

                        if (info.regularHolidays.isNotEmpty()) {
                            if (info.regularHolidays.any { it.weekInfo == 0 }) {
                                type = 1
                                info.regularHolidays.filter { it.weekInfo == 0 }.forEach {
                                    weeklyDays.add(mapDayStringToInt(it.dayOfWeek))
                                }
                            } else {
                                type = 2
                                info.regularHolidays.forEach {
                                    monthlyWeeks.add(it.weekInfo)
                                    monthlyDays.add(mapDayStringToInt(it.dayOfWeek))
                                }
                            }
                        }

                        val tempHoliday = info.temporaryHolidays.firstOrNull()
                        val isTempEnabled = tempHoliday != null
                        val tempStart = tempHoliday?.startDate?.replace("-", "/") ?: ""
                        val tempEnd = tempHoliday?.endDate?.replace("-", "/") ?: ""

                        val groupedSchedules =
                            info.openingHours.groupBy { "${it.startTime}-${it.endTime}" }
                        val scheduleItems = groupedSchedules.values.mapIndexed { index, hoursList ->
                            val first = hoursList.first()
                            val (sH, sM) = parseTime(first.startTime)
                            val (eH, eM) = parseTime(first.endTime)
                            val days = hoursList.map { mapDayStringToInt(it.dayOfWeek) }.toSet()

                            OperatingScheduleItem(
                                id = index.toLong(),
                                selectedDays = days,
                                startHour = sH, startMin = sM,
                                endHour = eH, endMin = eM
                            )
                        }
                        val finalSchedules = if (scheduleItems.isEmpty()) {
                            listOf(
                                OperatingScheduleItem(
                                    0,
                                    startHour = 10,
                                    startMin = 0,
                                    endHour = 22,
                                    endMin = 0
                                )
                            )
                        } else scheduleItems

                        currentState.copy(
                            operationState = currentState.operationState.copy(
                                regularHolidayType = type,
                                weeklyHolidayDays = weeklyDays,
                                monthlyHolidayWeeks = monthlyWeeks,
                                monthlyHolidayDays = monthlyDays,
                                isTempHolidayEnabled = isTempEnabled,
                                tempHolidayStart = tempStart,
                                tempHolidayEnd = tempEnd,
                                operatingSchedules = finalSchedules
                            )
                        )
                    }
                    checkSaveButtonEnabled()
                }
                .onFailure {
                    checkSaveButtonEnabled()
                }
        }
    }

    private fun fetchStorePhotos() {
        viewModelScope.launch {
            getStoreImagesUseCase()
                .onSuccess { images ->
                    val uiList = images.map { domain ->
                        StoreImageUiModel(
                            id = domain.id,
                            uri = domain.imageUrl,
                            isMain = domain.isMain,
                            isNew = false
                        )
                    }
                    _uiState.update { it.copy(photoState = it.photoState.copy(storePhotoList = uiList)) }
                }
                .onFailure {
                }
        }
    }

    private fun saveStorePhotos() {
        viewModelScope.launch {
            updateStoreImagesUseCase(_uiState.value.photoState.storePhotoList)
                .onSuccess {
                    _event.emit(StoreEditMainEvent.ShowToast("매장 사진이 수정되었습니다."))
                    fetchStorePhotos()
                }
                .onFailure {
                    _event.emit(StoreEditMainEvent.ShowToast("저장 실패: ${it.message}"))
                }
        }
    }

    private fun mapDayStringToInt(day: String): Int {
        return when (day.uppercase()) {
            "SUNDAY" -> 0; "MONDAY" -> 1; "TUESDAY" -> 2; "WEDNESDAY" -> 3; "THURSDAY" -> 4; "FRIDAY" -> 5; "SATURDAY" -> 6; else -> 1
        }
    }

    private fun parseTime(timeStr: String): Pair<Int, Int> {
        return try {
            val parts = timeStr.split(":")
            parts[0].toInt() to parts[1].toInt()
        } catch (e: Exception) {
            0 to 0
        }
    }

    private fun mapIndexToDayOfWeek(index: Int): String {
        return when (index) {
            0 -> "SUNDAY"; 1 -> "MONDAY"; 2 -> "TUESDAY"; 3 -> "WEDNESDAY"; 4 -> "THURSDAY"; 5 -> "FRIDAY"; 6 -> "SATURDAY"; else -> "MONDAY"
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun onAction(action: StoreEditAction) {
        when (action) {
            is StoreEditAction.ConfirmAddMenu -> {
                saveMenuItem(
                    menuId = null,
                    categoryId = action.categoryId,
                    name = action.name,
                    priceString = action.price,
                    imageUri = action.imageUri
                )
            }
            is StoreEditAction.UpdateMenuItem -> {
                saveMenuItem(
                    menuId = action.updatedItem.id,
                    categoryId = action.newCategoryId,
                    name = action.updatedItem.name,
                    priceString = action.updatedItem.price,
                    imageUri = action.updatedItem.imageUrl
                )
            }
            is StoreEditAction.OpenEditMenu -> {
                _uiState.update { it.copy(menuState = it.menuState.copy(editingMenuItem = action.categoryId to action.item)) }
            }
            is StoreEditAction.DismissEditMenu -> {
                _uiState.update { it.copy(menuState = it.menuState.copy(editingMenuItem = null)) }
            }
            is StoreEditAction.DeleteMenuItem -> {
                deleteMenuItem(action.categoryId, action.itemId)
            }
            is StoreEditAction.OpenAddMenu -> {
                _uiState.update { it.copy(menuState = it.menuState.copy(addingMenuCategoryId = action.categoryId)) }
            }
            is StoreEditAction.DismissAddMenu -> {
                _uiState.update { it.copy(menuState = it.menuState.copy(addingMenuCategoryId = null)) }
            }
            is StoreEditAction.OpenAddCategoryDialog -> {
                _uiState.update { it.copy(menuState = it.menuState.copy(isAddingCategory = true)) }
            }
            is StoreEditAction.DismissAddCategoryDialog -> {
                _uiState.update { it.copy(menuState = it.menuState.copy(isAddingCategory = false)) }
            }
            is StoreEditAction.ConfirmAddCategory -> confirmAddCategory(action.name)
            is StoreEditAction.OpenRenameDialog -> {
                _uiState.update { it.copy(menuState = it.menuState.copy(editingCategory = action.category)) }
            }
            is StoreEditAction.DismissRenameDialog -> {
                _uiState.update { it.copy(menuState = it.menuState.copy(editingCategory = null)) }
            }
            is StoreEditAction.UpdateCategoryName -> updateCategoryName(
                action.categoryId,
                action.newName
            )
            is StoreEditAction.SetCategoryEditMode -> {
                _uiState.update { it.copy(menuState = it.menuState.copy(isCategoryEditMode = action.isEdit)) }
            }
            is StoreEditAction.ToggleRegularHolidayType -> {
                _uiState.update {
                    val newType = if (it.operationState.regularHolidayType == action.type) 0 else action.type
                    it.copy(operationState = it.operationState.copy(regularHolidayType = newType))
                }
            }
            is StoreEditAction.UpdateWeeklyHolidays -> {
                _uiState.update { state ->
                    val newHolidays = action.days
                    val cleanedSchedules = state.operationState.operatingSchedules.map { schedule ->
                        val newSelectedDays = schedule.selectedDays - newHolidays
                        schedule.copy(selectedDays = newSelectedDays)
                    }
                    state.copy(
                        operationState = state.operationState.copy(
                            weeklyHolidayDays = newHolidays,
                            operatingSchedules = cleanedSchedules
                        ),
                        dialogState = state.dialogState.copy(showWeeklyDayDialog = false)
                    )
                }
            }
            is StoreEditAction.UpdateMonthlyWeeks -> {
                _uiState.update {
                    it.copy(
                        operationState = it.operationState.copy(monthlyHolidayWeeks = action.weeks),
                        dialogState = it.dialogState.copy(showMonthlyWeekDialog = false)
                    )
                }
            }
            is StoreEditAction.UpdateMonthlyDays -> {
                _uiState.update {
                    it.copy(
                        operationState = it.operationState.copy(monthlyHolidayDays = action.days),
                        dialogState = it.dialogState.copy(showMonthlyDayDialog = false)
                    )
                }
            }
            is StoreEditAction.SetWeeklyDialogVisible -> _uiState.update {
                it.copy(dialogState = it.dialogState.copy(showWeeklyDayDialog = action.visible))
            }
            is StoreEditAction.SetMonthlyWeekDialogVisible -> _uiState.update {
                it.copy(dialogState = it.dialogState.copy(showMonthlyWeekDialog = action.visible))
            }
            is StoreEditAction.SetMonthlyDayDialogVisible -> _uiState.update {
                it.copy(dialogState = it.dialogState.copy(showMonthlyDayDialog = action.visible))
            }
            is StoreEditAction.SetTempHolidayDatePickerVisible -> _uiState.update {
                it.copy(dialogState = it.dialogState.copy(showTempHolidayDatePicker = action.visible))
            }
            is StoreEditAction.ToggleTempHoliday -> _uiState.update { 
                it.copy(operationState = it.operationState.copy(isTempHolidayEnabled = !it.operationState.isTempHolidayEnabled)) 
            }
            is StoreEditAction.UpdateTempHolidayRange -> _uiState.update {
                it.copy(
                    operationState = it.operationState.copy(
                        tempHolidayStart = action.start,
                        tempHolidayEnd = action.end
                    ),
                    dialogState = it.dialogState.copy(showTempHolidayDatePicker = false)
                )
            }
            is StoreEditAction.AddOperatingSchedule -> {
                val newId = (_uiState.value.operationState.operatingSchedules.maxOfOrNull { it.id } ?: 0) + 1
                val newItem = OperatingScheduleItem(
                    newId,
                    startHour = 10,
                    startMin = 0,
                    endHour = 22,
                    endMin = 0
                )
                _uiState.update { it.copy(operationState = it.operationState.copy(operatingSchedules = it.operationState.operatingSchedules + newItem)) }
            }
            is StoreEditAction.UpdateOperatingDays -> updateOperatingDays(action.id, action.dayIdx)
            is StoreEditAction.UpdateOperatingTime -> {
                _uiState.update { state ->
                    val updatedList = state.operationState.operatingSchedules.map { item ->
                        if (item.id == action.id) item.copy(
                            startHour = action.startHour,
                            startMin = action.startMin,
                            endHour = action.endHour,
                            endMin = action.endMin
                        ) else item
                    }
                    state.copy(operationState = state.operationState.copy(operatingSchedules = updatedList))
                }
            }
            is StoreEditAction.RemoveOperatingSchedule -> {
                _uiState.update { it.copy(operationState = it.operationState.copy(operatingSchedules = it.operationState.operatingSchedules.filter { item -> item.id != action.id })) }
            }
            is StoreEditAction.MoveMenuItem -> {
                val allCategories = _uiState.value.menuState.menuCategories.toMutableList()
                val categoryIndex = allCategories.indexOfFirst { it.id == action.categoryId }

                if (categoryIndex != -1) {
                    val targetCategory = allCategories[categoryIndex]
                    val menuList = targetCategory.items.toMutableList()
                    if (action.fromIndex in menuList.indices && action.toIndex in menuList.indices) {
                        Collections.swap(menuList, action.fromIndex, action.toIndex)
                        allCategories[categoryIndex] = targetCategory.copy(items = menuList)
                        _uiState.update { it.copy(menuState = it.menuState.copy(menuCategories = allCategories)) }
                    }
                }
            }
            is StoreEditAction.MoveCategory -> {
                val currentList = _uiState.value.menuState.menuCategories.toMutableList()
                if (action.fromIndex in currentList.indices && action.toIndex in currentList.indices) {
                    Collections.swap(currentList, action.fromIndex, action.toIndex)
                    _uiState.update { it.copy(menuState = it.menuState.copy(menuCategories = currentList)) }
                }
            }
            is StoreEditAction.DeleteCategory -> deleteCategory(action.categoryId)
            is StoreEditAction.AddCategory -> addCategory()
            is StoreEditAction.SaveCategories -> saveCategories()
            is StoreEditAction.AddStorePhotos -> {
                val current = _uiState.value.photoState.storePhotoList.toMutableList()
                val newItems = action.uris.map { uri ->
                    StoreImageUiModel(
                        id = null,
                        uri = uri.toString(),
                        isMain = false,
                        isNew = true
                    )
                }

                if (current.size + newItems.size <= 5) {
                    current.addAll(newItems)
                    if (current.none { it.isMain } && current.isNotEmpty()) {
                        current[0] = current[0].copy(isMain = true)
                    }
                    _uiState.update { it.copy(photoState = it.photoState.copy(storePhotoList = current), isSaveButtonEnabled = true) }
                }
            }
            is StoreEditAction.RemoveStorePhoto -> {
                val targetUri = action.uriString
                val current = _uiState.value.photoState.storePhotoList.toMutableList()
                val wasMain = current.find { it.uri == targetUri }?.isMain == true

                current.removeAll { it.uri == targetUri }

                if (wasMain && current.isNotEmpty()) {
                    current[0] = current[0].copy(isMain = true)
                }
                _uiState.update { it.copy(photoState = it.photoState.copy(storePhotoList = current), isSaveButtonEnabled = true) }
            }
            is StoreEditAction.SetRepresentativePhoto -> {
                val targetUri = action.uriString
                val updated = _uiState.value.photoState.storePhotoList.map {
                    it.copy(isMain = (it.uri == targetUri))
                }
                _uiState.update { it.copy(photoState = it.photoState.copy(storePhotoList = updated), isSaveButtonEnabled = true) }
            }
            is StoreEditAction.SaveStorePhotos -> saveStorePhotos()
        }
        checkSaveButtonEnabled()
    }

    private fun updateOperatingDays(id: Long, dayIdx: Int) {
        val currentSchedules = _uiState.value.operationState.operatingSchedules
        val targetItem = currentSchedules.find { it.id == id } ?: return

        val isOccupiedByOther = currentSchedules.any { item ->
            item.id != id && item.selectedDays.contains(dayIdx)
        }

        if (isOccupiedByOther && !targetItem.selectedDays.contains(dayIdx)) {
            viewModelScope.launch {
                _event.emit(StoreEditMainEvent.ShowToast("이미 설정된 요일입니다."))
            }
            return
        }

        _uiState.update { state ->
            val updatedList = state.operationState.operatingSchedules.map { item ->
                if (item.id == id) {
                    val currentDays = item.selectedDays
                    val newDays =
                        if (currentDays.contains(dayIdx)) currentDays - dayIdx else currentDays + dayIdx
                    item.copy(selectedDays = newDays)
                } else item
            }
            state.copy(operationState = state.operationState.copy(operatingSchedules = updatedList))
        }
    }

    private fun checkSaveButtonEnabled() {
        val state = _uiState.value

        val isSchedulesValid = state.operationState.operatingSchedules.isNotEmpty() &&
                state.operationState.operatingSchedules.all { it.selectedDays.isNotEmpty() }

        val isRegularHolidayValid = when (state.operationState.regularHolidayType) {
            1 -> state.operationState.weeklyHolidayDays.isNotEmpty()
            2 -> state.operationState.monthlyHolidayWeeks.isNotEmpty() && state.operationState.monthlyHolidayDays.isNotEmpty()
            else -> true
        }

        val isTempHolidayValid = if (state.operationState.isTempHolidayEnabled) {
            state.operationState.tempHolidayStart.isNotBlank() && state.operationState.tempHolidayEnd.isNotBlank()
        } else {
            true
        }

        val isEnabled = isSchedulesValid && isRegularHolidayValid && isTempHolidayValid
        _uiState.update { it.copy(isSaveButtonEnabled = isEnabled) }
    }

    private fun deleteCategory(categoryId: Long) {
        _uiState.update { state ->
            state.copy(menuState = state.menuState.copy(menuCategories = state.menuState.menuCategories.filter { it.id != categoryId }))
        }
    }

    private fun addCategory() {
        _uiState.update { state ->
            val newId = (state.menuState.menuCategories.maxOfOrNull { it.id } ?: 0) + 1
            val newCategory = StoreMenuCategory(id = newId, name = "새 카테고리 ${newId}")
            state.copy(menuState = state.menuState.copy(menuCategories = state.menuState.menuCategories + newCategory))
        }
    }

    private fun saveCategories() {
        val currentCategories = _uiState.value.menuState.menuCategories

        viewModelScope.launch {
            updateMenuCategoriesUseCase(currentCategories)
                .onSuccess {
                    _event.emit(StoreEditMainEvent.ShowToast("메뉴 카테고리가 저장되었습니다."))
                    _uiState.update { it.copy(menuState = it.menuState.copy(isCategoryEditMode = false)) }
                }
                .onFailure { e ->
                    _event.emit(StoreEditMainEvent.ShowToast("저장 실패: ${e.message}"))
                }
        }
    }

    private fun updateCategoryName(categoryId: Long, newName: String) {
        _uiState.update { state ->
            val updatedList = state.menuState.menuCategories.map {
                if (it.id == categoryId) it.copy(name = newName) else it
            }
            state.copy(
                menuState = state.menuState.copy(
                    menuCategories = updatedList,
                    editingCategory = null
                )
            )
        }
    }

    private fun confirmAddCategory(name: String) {
        _uiState.update { state ->
            val minId = state.menuState.menuCategories.minOfOrNull { it.id } ?: 0
            val newFakeId = if (minId < 0) minId - 1 else -1

            val newCategory = StoreMenuCategory(id = newFakeId, name = name, items = emptyList())

            state.copy(
                menuState = state.menuState.copy(
                    menuCategories = state.menuState.menuCategories + newCategory,
                    isAddingCategory = false
                )
            )
        }
    }

    private fun saveMenuItem(
        menuId: Long?,
        categoryId: Long,
        name: String,
        priceString: String,
        imageUri: String?
    ) {
        viewModelScope.launch {
            val price = try {
                priceString.replace(",", "").toInt()
            } catch (e: Exception) { 0 }

            val isImageChanged = imageUri == null || !imageUri.startsWith("http")

            saveMenuUseCase(
                menuId = menuId,
                categoryId = categoryId,
                name = name,
                price = price,
                imageUri = imageUri,
                isImageChanged = isImageChanged
            )
                .onSuccess {
                    _event.emit(StoreEditMainEvent.ShowToast("메뉴가 저장되었습니다."))

                    _uiState.update {
                        it.copy(
                            menuState = it.menuState.copy(
                                addingMenuCategoryId = null,
                                editingMenuItem = null
                            )
                        )
                    }
                    loadStoreMenuData()
                }
                .onFailure { e ->
                    _event.emit(StoreEditMainEvent.ShowToast("저장 실패: ${e.message}"))
                }
        }
    }

    private fun deleteMenuItem(categoryId: Long, itemId: Long) {
        viewModelScope.launch {
            if (itemId < 0) {
                removeMenuItemFromState(categoryId, itemId)
                _event.emit(StoreEditMainEvent.ShowToast("작성 중인 메뉴가 삭제되었습니다."))
                return@launch
            }

            deleteMenuUseCase(itemId)
                .onSuccess {
                    removeMenuItemFromState(categoryId, itemId)
                    _event.emit(StoreEditMainEvent.ShowToast("메뉴가 성공적으로 삭제되었습니다."))
                }
                .onFailure { e ->
                    _event.emit(StoreEditMainEvent.ShowToast("삭제 실패: ${e.message}"))
                }
        }
    }

    private fun removeMenuItemFromState(categoryId: Long, itemId: Long) {
        _uiState.update { state ->
            val updatedCategories = state.menuState.menuCategories.map { cat ->
                if (cat.id == categoryId) {
                    cat.copy(items = cat.items.filter { it.id != itemId })
                } else cat
            }
            state.copy(menuState = state.menuState.copy(menuCategories = updatedCategories, editingMenuItem = null))
        }
    }

    fun onSaveClick() {
        val state = _uiState.value

        viewModelScope.launch {
            val regularHolidays = when (state.operationState.regularHolidayType) {
                1 -> state.operationState.weeklyHolidayDays.map { dayIdx ->
                    RegularHoliday(
                        dayOfWeek = mapIndexToDayOfWeek(
                            dayIdx
                        ), weekInfo = 0
                    )
                }
                2 -> state.operationState.monthlyHolidayWeeks.flatMap { week ->
                    state.operationState.monthlyHolidayDays.map { dayIdx ->
                        RegularHoliday(
                            dayOfWeek = mapIndexToDayOfWeek(dayIdx),
                            weekInfo = week
                        )
                    }
                }
                else -> emptyList()
            }
            val tempHolidays =
                if (state.operationState.isTempHolidayEnabled && state.operationState.tempHolidayStart.isNotBlank()) {
                    listOf(
                        TemporaryHoliday(
                            startDate = state.operationState.tempHolidayStart.replace("/", "-"),
                            endDate = state.operationState.tempHolidayEnd.replace("/", "-")
                        )
                    )
                } else {
                    emptyList()
                }
            val openingHours = state.operationState.operatingSchedules.flatMap { schedule ->
                schedule.selectedDays.map { dayIdx ->
                    OpeningHour(
                        dayOfWeek = mapIndexToDayOfWeek(dayIdx),
                        startTime = "${
                            schedule.startHour.toString().padStart(2, '0')
                        }:${schedule.startMin.toString().padStart(2, '0')}",
                        endTime = "${
                            schedule.endHour.toString().padStart(2, '0')
                        }:${schedule.endMin.toString().padStart(2, '0')}"
                    )
                }
            }

            val operationDeferred = async {
                updateStoreOperationInfoUseCase(regularHolidays, tempHolidays, openingHours)
            }
            val menuDeferred = async {
                updateMenuOrdersUseCase(state.menuState.menuCategories)
            }
            val photoDeferred = async {
                updateStoreImagesUseCase(state.photoState.storePhotoList)
            }

            val operationResult = operationDeferred.await()
            val menuResult = menuDeferred.await()
            val photoResult = photoDeferred.await()

            if (operationResult.isSuccess && menuResult.isSuccess) {
                _event.emit(StoreEditMainEvent.ShowToast("저장되었습니다."))

                loadStoreMenuData()
                loadOperationInfo()
                fetchStorePhotos()

                _event.emit(StoreEditMainEvent.NavigateBack)
            } else {
                val opError = operationResult.exceptionOrNull()?.message
                val menuError = menuResult.exceptionOrNull()?.message
                val photoError = photoResult.exceptionOrNull()?.message

                val errorMsg = listOfNotNull(
                    if (operationResult.isFailure) "운영정보: $opError" else null,
                    if (menuResult.isFailure) "카테고리: $menuError" else null,
                    if (photoResult.isFailure) "사진: $photoError" else null
                ).joinToString(", ")

                _event.emit(StoreEditMainEvent.ShowToast("저장 실패: $errorMsg"))
            }
        }
    }
}