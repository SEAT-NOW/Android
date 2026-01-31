package com.gmg.seatnow.presentation.owner.store.mypage.storeManage.storeManageEdit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.OperatingScheduleItem
import com.gmg.seatnow.domain.model.OpeningHour
import com.gmg.seatnow.domain.model.RegularHoliday
import com.gmg.seatnow.domain.model.StoreMenuCategory
import com.gmg.seatnow.domain.model.TemporaryHoliday
import com.gmg.seatnow.domain.usecase.store.GetStoreOperationInfoUseCase
import com.gmg.seatnow.domain.usecase.store.UpdateStoreOperationInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val updateStoreOperationInfoUseCase: UpdateStoreOperationInfoUseCase
) : ViewModel() {

    // --- UI State ---
    data class StoreEditUiState(
        val selectedTabIndex: Int = 0,
        val isSaveButtonEnabled: Boolean = false,

        val isCategoryEditMode: Boolean = false,

        // 운영 정보
        val regularHolidayType: Int = 0, // 0:없음, 1:매주, 2:매월
        val weeklyHolidayDays: Set<Int> = emptySet(),
        val monthlyHolidayWeeks: Set<Int> = emptySet(),
        val monthlyHolidayDays: Set<Int> = emptySet(),

        val isTempHolidayEnabled: Boolean = false,
        val tempHolidayStart: String = "",
        val tempHolidayEnd: String = "",

        val operatingSchedules: List<OperatingScheduleItem> = emptyList(),

        // Dialog Visibilities
        val showWeeklyDayDialog: Boolean = false,
        val showMonthlyWeekDialog: Boolean = false,
        val showMonthlyDayDialog: Boolean = false,
        val showTempHolidayDatePicker: Boolean = false,

        val menuCategories: List<StoreMenuCategory> = emptyList()
    )

    private val _uiState = MutableStateFlow(StoreEditUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<StoreEditMainEvent>()
    val event = _event.asSharedFlow()

    init {
        loadOperationInfo()
        initializeMenuData()
    }

    private fun initializeMenuData() {
        // 추후 API 호출로 대체될 부분
        // 만약 데이터가 없다면 Default 3개 카테고리 생성
        val currentCategories = _uiState.value.menuCategories
        if (currentCategories.isEmpty()) {
            val defaultCategories = listOf(
                StoreMenuCategory(id = 1, name = "메인메뉴", items = emptyList()),
                StoreMenuCategory(id = 2, name = "사이드메뉴", items = emptyList()),
                StoreMenuCategory(id = 3, name = "주류", items = emptyList())
            )
            _uiState.update { it.copy(menuCategories = defaultCategories) }
        }
    }

    private fun loadOperationInfo() {
        viewModelScope.launch {
            getStoreOperationInfoUseCase()
                .onSuccess { info ->
                    _uiState.update { currentState ->
                        // 1. 정기 휴무일 매핑
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

                        // 2. 임시 휴무일 매핑
                        val tempHoliday = info.temporaryHolidays.firstOrNull()
                        val isTempEnabled = tempHoliday != null
                        val tempStart = tempHoliday?.startDate?.replace("-", "/") ?: ""
                        val tempEnd = tempHoliday?.endDate?.replace("-", "/") ?: ""

                        // 3. 운영 시간 매핑
                        val groupedSchedules = info.openingHours.groupBy { "${it.startTime}-${it.endTime}" }
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
                            listOf(OperatingScheduleItem(0, startHour = 10, startMin = 0, endHour = 22, endMin = 0))
                        } else scheduleItems

                        currentState.copy(
                            regularHolidayType = type,
                            weeklyHolidayDays = weeklyDays,
                            monthlyHolidayWeeks = monthlyWeeks,
                            monthlyHolidayDays = monthlyDays,
                            isTempHolidayEnabled = isTempEnabled,
                            tempHolidayStart = tempStart,
                            tempHolidayEnd = tempEnd,
                            operatingSchedules = finalSchedules
                        )
                    }
                    checkSaveButtonEnabled()
                }
                .onFailure {
                    checkSaveButtonEnabled()
                }
        }
    }

    // Helper Functions
    private fun mapDayStringToInt(day: String): Int {
        return when (day.uppercase()) {
            "SUNDAY" -> 0; "MONDAY" -> 1; "TUESDAY" -> 2; "WEDNESDAY" -> 3; "THURSDAY" -> 4; "FRIDAY" -> 5; "SATURDAY" -> 6; else -> 1
        }
    }

    private fun parseTime(timeStr: String): Pair<Int, Int> {
        return try {
            val parts = timeStr.split(":")
            parts[0].toInt() to parts[1].toInt()
        } catch (e: Exception) { 0 to 0 }
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
            is StoreEditAction.SetCategoryEditMode -> {
                _uiState.update { it.copy(isCategoryEditMode = action.isEdit) }
            }
            is StoreEditAction.ToggleRegularHolidayType -> {
                _uiState.update {
                    val newType = if (it.regularHolidayType == action.type) 0 else action.type
                    // 타입 변경 시 기존 휴무일 데이터 초기화 여부는 기획에 따라 결정 (여기선 유지)
                    it.copy(regularHolidayType = newType)
                }
            }

            // ★ [핵심 2] 매주 휴무일 업데이트 시 -> 운영 스케줄과 겹치는 요일 자동 해제 로직 추가
            is StoreEditAction.UpdateWeeklyHolidays -> {
                _uiState.update { state ->
                    val newHolidays = action.days

                    // 기존 스케줄에서, 새로 지정된 휴무일과 겹치는 요일을 제거
                    val cleanedSchedules = state.operatingSchedules.map { schedule ->
                        // (기존 선택 요일) - (새로운 휴무 요일)
                        val newSelectedDays = schedule.selectedDays - newHolidays
                        schedule.copy(selectedDays = newSelectedDays)
                    }

                    state.copy(
                        weeklyHolidayDays = newHolidays,
                        operatingSchedules = cleanedSchedules, // 정제된 스케줄 반영
                        showWeeklyDayDialog = false
                    )
                }
            }

            is StoreEditAction.UpdateMonthlyWeeks -> {
                _uiState.update { it.copy(monthlyHolidayWeeks = action.weeks, showMonthlyWeekDialog = false) }
            }
            is StoreEditAction.UpdateMonthlyDays -> {
                _uiState.update { it.copy(monthlyHolidayDays = action.days, showMonthlyDayDialog = false) }
            }
            is StoreEditAction.SetWeeklyDialogVisible -> _uiState.update { it.copy(showWeeklyDayDialog = action.visible) }
            is StoreEditAction.SetMonthlyWeekDialogVisible -> _uiState.update { it.copy(showMonthlyWeekDialog = action.visible) }
            is StoreEditAction.SetMonthlyDayDialogVisible -> _uiState.update { it.copy(showMonthlyDayDialog = action.visible) }
            is StoreEditAction.SetTempHolidayDatePickerVisible -> _uiState.update { it.copy(showTempHolidayDatePicker = action.visible) }
            is StoreEditAction.ToggleTempHoliday -> _uiState.update { it.copy(isTempHolidayEnabled = !it.isTempHolidayEnabled) }
            is StoreEditAction.UpdateTempHolidayRange -> _uiState.update {
                it.copy(tempHolidayStart = action.start, tempHolidayEnd = action.end, showTempHolidayDatePicker = false)
            }
            is StoreEditAction.AddOperatingSchedule -> {
                val newId = (_uiState.value.operatingSchedules.maxOfOrNull { it.id } ?: 0) + 1
                val newItem = OperatingScheduleItem(newId, startHour = 10, startMin = 0, endHour = 22, endMin = 0)
                _uiState.update { it.copy(operatingSchedules = it.operatingSchedules + newItem) }
            }

            // ★ [핵심 1] 운영 스케줄 요일 선택 시 중복 방지 로직 (OwnerSignUpViewModel 복원)
            is StoreEditAction.UpdateOperatingDays -> updateOperatingDays(action.id, action.dayIdx)

            is StoreEditAction.UpdateOperatingTime -> {
                _uiState.update { state ->
                    val updatedList = state.operatingSchedules.map { item ->
                        if (item.id == action.id) item.copy(startHour = action.startHour, startMin = action.startMin, endHour = action.endHour, endMin = action.endMin) else item
                    }
                    state.copy(operatingSchedules = updatedList)
                }
            }
            is StoreEditAction.RemoveOperatingSchedule -> {
                _uiState.update { it.copy(operatingSchedules = it.operatingSchedules.filter { item -> item.id != action.id }) }
            }
            is StoreEditAction.MoveMenuItem -> moveMenuItem(action.categoryId, action.fromIndex, action.toIndex)
            is StoreEditAction.MoveCategory -> moveCategory(action.fromIndex, action.toIndex)
            is StoreEditAction.DeleteCategory -> deleteCategory(action.categoryId)
            is StoreEditAction.AddCategory -> addCategory()
            is StoreEditAction.SaveCategories -> saveCategories()
        }
        // ★ 상태 변경 후 즉시 유효성 검사
        checkSaveButtonEnabled()
    }

    // ★ [핵심 1 구현] 중복 요일 선택 방지 로직
    private fun updateOperatingDays(id: Long, dayIdx: Int) {
        val currentSchedules = _uiState.value.operatingSchedules
        val targetItem = currentSchedules.find { it.id == id } ?: return

        // 다른 스케줄에서 이미 이 요일을 쓰고 있는지 확인
        val isOccupiedByOther = currentSchedules.any { item ->
            item.id != id && item.selectedDays.contains(dayIdx)
        }

        // 이미 다른 곳에서 쓰고 있고, 현재 아이템에 없는 상태라면 -> 중복 선택 시도임
        if (isOccupiedByOther && !targetItem.selectedDays.contains(dayIdx)) {
            viewModelScope.launch {
                _event.emit(StoreEditMainEvent.ShowToast("이미 설정된 요일입니다."))
            }
            return
        }

        // 중복이 아니면 업데이트 진행
        _uiState.update { state ->
            val updatedList = state.operatingSchedules.map { item ->
                if (item.id == id) {
                    val currentDays = item.selectedDays
                    val newDays = if (currentDays.contains(dayIdx)) currentDays - dayIdx else currentDays + dayIdx
                    item.copy(selectedDays = newDays)
                } else item
            }
            state.copy(operatingSchedules = updatedList)
        }
    }

    // ★ [핵심 3] 저장 버튼 활성화 로직 (OwnerSignUpViewModel Step4 로직 + 휴무일 유효성)
    private fun checkSaveButtonEnabled() {
        val state = _uiState.value

        // 1. 운영 스케줄 유효성: 스케줄이 하나라도 있어야 하고, 존재하는 모든 스케줄은 최소 1개 이상의 요일이 선택되어야 함.
        // (SignUpViewModel 로직: operatingSchedules.isNotEmpty() && all { it.selectedDays.isNotEmpty() })
        val isSchedulesValid = state.operatingSchedules.isNotEmpty() &&
                state.operatingSchedules.all { it.selectedDays.isNotEmpty() }

        // 2. 정기 휴무 설정 유효성
        val isRegularHolidayValid = when (state.regularHolidayType) {
            1 -> state.weeklyHolidayDays.isNotEmpty() // 매주: 요일 선택 필수
            2 -> state.monthlyHolidayWeeks.isNotEmpty() && state.monthlyHolidayDays.isNotEmpty() // 매월: 주차 & 요일 필수
            else -> true // 없음: 통과
        }

        // 3. 임시 휴무 설정 유효성
        val isTempHolidayValid = if (state.isTempHolidayEnabled) {
            state.tempHolidayStart.isNotBlank() && state.tempHolidayEnd.isNotBlank()
        } else {
            true
        }

        // 모든 조건 만족 시 활성화
        val isEnabled = isSchedulesValid && isRegularHolidayValid && isTempHolidayValid
        _uiState.update { it.copy(isSaveButtonEnabled = isEnabled) }
    }

    // ★ [로직] 카테고리 순서 변경
    private fun moveCategory(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val mutableCategories = state.menuCategories.toMutableList()
            if (fromIndex in mutableCategories.indices && toIndex in mutableCategories.indices) {
                if (fromIndex < toIndex) {
                    for (i in fromIndex until toIndex) {
                        Collections.swap(mutableCategories, i, i + 1)
                    }
                } else {
                    for (i in fromIndex downTo toIndex + 1) {
                        Collections.swap(mutableCategories, i, i - 1)
                    }
                }
            }
            state.copy(menuCategories = mutableCategories)
        }
    }

    // ★ [로직] 카테고리 삭제
    private fun deleteCategory(categoryId: Long) {
        _uiState.update { state ->
            state.copy(menuCategories = state.menuCategories.filter { it.id != categoryId })
        }
    }

    // ★ [로직] 카테고리 추가 (임시)
    private fun addCategory() {
        _uiState.update { state ->
            val newId = (state.menuCategories.maxOfOrNull { it.id } ?: 0) + 1
            val newCategory = StoreMenuCategory(id = newId, name = "새 카테고리 ${newId}")
            state.copy(menuCategories = state.menuCategories + newCategory)
        }
    }

    // ★ [로직] 카테고리 저장 (API 호출용 Placeholder)
    private fun saveCategories() {
        viewModelScope.launch {
            // TODO: API 호출 (PATCH)

            _event.emit(StoreEditMainEvent.ShowToast("저장되었습니다."))

            // ★ NavigateBack 이벤트를 쏘지 않고, 편집 모드 상태만 해제합니다.
            // 이렇게 하면 현재의 CategoryEditScreen(Overlay)이 사라지고
            // 뒤에 있던 StoreEditMainScreen이 그대로 보입니다.
            _uiState.update { it.copy(isCategoryEditMode = false) }
        }
    }

    private fun moveMenuItem(categoryId: Long, fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val updatedCategories = state.menuCategories.map { category ->
                if (category.id == categoryId) {
                    val mutableItems = category.items.toMutableList()
                    // 인덱스 유효성 검사
                    if (fromIndex in mutableItems.indices && toIndex in mutableItems.indices) {
                        if (fromIndex < toIndex) {
                            for (i in fromIndex until toIndex) {
                                Collections.swap(mutableItems, i, i + 1)
                            }
                        } else {
                            for (i in fromIndex downTo toIndex + 1) {
                                Collections.swap(mutableItems, i, i - 1)
                            }
                        }
                    }
                    category.copy(items = mutableItems)
                } else {
                    category
                }
            }
            state.copy(menuCategories = updatedCategories)
        }
    }

    fun onSaveClick() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.selectedTabIndex == 0) {
                // 1. 정기 휴무일
                val regularHolidays = when (state.regularHolidayType) {
                    1 -> state.weeklyHolidayDays.map { dayIdx ->
                        RegularHoliday(dayOfWeek = mapIndexToDayOfWeek(dayIdx), weekInfo = 0)
                    }
                    2 -> state.monthlyHolidayWeeks.flatMap { week ->
                        state.monthlyHolidayDays.map { dayIdx ->
                            RegularHoliday(dayOfWeek = mapIndexToDayOfWeek(dayIdx), weekInfo = week)
                        }
                    }
                    else -> emptyList()
                }

                // 2. 임시 휴무일
                val tempHolidays = if (state.isTempHolidayEnabled && state.tempHolidayStart.isNotBlank()) {
                    listOf(
                        TemporaryHoliday(
                            startDate = state.tempHolidayStart.replace("/", "-"),
                            endDate = state.tempHolidayEnd.replace("/", "-")
                        )
                    )
                } else {
                    emptyList()
                }

                // 3. 운영 시간 (자동 해제 로직이 이미 updateWeeklyHolidays에서 동작했으므로 그대로 전송 가능)
                // 혹시 모를 방어 코드로 한 번 더 필터링 가능하지만, 위에서 처리했으므로 바로 매핑
                val openingHours = state.operatingSchedules.flatMap { schedule ->
                    schedule.selectedDays.map { dayIdx ->
                        OpeningHour(
                            dayOfWeek = mapIndexToDayOfWeek(dayIdx),
                            startTime = "${schedule.startHour.toString().padStart(2, '0')}:${schedule.startMin.toString().padStart(2, '0')}",
                            endTime = "${schedule.endHour.toString().padStart(2, '0')}:${schedule.endMin.toString().padStart(2, '0')}"
                        )
                    }
                }

                // 4. API 호출
                updateStoreOperationInfoUseCase(regularHolidays, tempHolidays, openingHours)
                    .onSuccess {
                        _event.emit(StoreEditMainEvent.ShowToast("저장되었습니다."))
                        _event.emit(StoreEditMainEvent.NavigateBack)
                    }
                    .onFailure {
                        _event.emit(StoreEditMainEvent.ShowToast("저장 실패: ${it.message}"))
                    }
            }
        }
    }
}

sealed interface StoreEditMainEvent {
    data object NavigateBack : StoreEditMainEvent
    data class ShowToast(val message: String) : StoreEditMainEvent
}

// Action 정의
sealed interface StoreEditAction {
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
}