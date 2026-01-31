package com.gmg.seatnow.presentation.owner.store.mypage.storeManage.storeManageEdit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.OpeningHour
import com.gmg.seatnow.domain.model.OperatingScheduleItem
import com.gmg.seatnow.domain.model.RegularHoliday
import com.gmg.seatnow.domain.model.TemporaryHoliday
import com.gmg.seatnow.domain.usecase.store.GetStoreOperationInfoUseCase
import com.gmg.seatnow.domain.usecase.store.UpdateStoreOperationInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreEditMainViewModel @Inject constructor(
    private val getStoreOperationInfoUseCase: GetStoreOperationInfoUseCase,
    private val updateStoreOperationInfoUseCase: UpdateStoreOperationInfoUseCase
) : ViewModel() {

    // --- UI State ---
    data class StoreEditUiState(
        // [기본 상태]
        val selectedTabIndex: Int = 0,
        val isSaveButtonEnabled: Boolean = false, // 저장 버튼 활성화 여부

        // [운영 정보 데이터 - Step4 로직 이식]
        val regularHolidayType: Int = 0, // 0: 없음, 1: 매주, 2: 매월
        val weeklyHolidayDays: Set<Int> = emptySet(),
        val monthlyHolidayWeeks: Set<Int> = emptySet(),
        val monthlyHolidayDays: Set<Int> = emptySet(),

        val isTempHolidayEnabled: Boolean = false,
        val tempHolidayStart: String = "",
        val tempHolidayEnd: String = "",

        // 기본값: 18:00 ~ 00:00 (Step4와 동일하게 초기화)
        val operatingSchedules: List<OperatingScheduleItem> = emptyList(),

        // 다이얼로그 가시성 상태
        val showWeeklyDayDialog: Boolean = false,
        val showMonthlyWeekDialog: Boolean = false,
        val showMonthlyDayDialog: Boolean = false,
        val showTempHolidayDatePicker: Boolean = false
    )

    private val _uiState = MutableStateFlow(StoreEditUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<StoreEditMainEvent>()
    val event = _event.asSharedFlow()

    init {
        loadOperationInfo()
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
                            // weekInfo가 0인 항목이 하나라도 있으면 '매주'로 간주 (혹은 데이터 구조에 따라 우선순위 결정)
                            if (info.regularHolidays.any { it.weekInfo == 0 }) {
                                type = 1
                                info.regularHolidays.filter { it.weekInfo == 0 }.forEach {
                                    weeklyDays.add(mapDayStringToInt(it.dayOfWeek))
                                }
                            } else {
                                type = 2
                                info.regularHolidays.forEach {
                                    monthlyWeeks.add(it.weekInfo) // 10(마지막주)도 그대로 저장
                                    monthlyDays.add(mapDayStringToInt(it.dayOfWeek))
                                }
                            }
                        }

                        // 2. 임시 휴무일 매핑 (첫 번째 항목만 사용)
                        val tempHoliday = info.temporaryHolidays.firstOrNull()
                        val isTempEnabled = tempHoliday != null
                        val tempStart = tempHoliday?.startDate?.replace("-", "/") ?: ""
                        val tempEnd = tempHoliday?.endDate?.replace("-", "/") ?: ""

                        // 3. 운영 시간 매핑
                        // 같은 시간대(Start~End)를 가진 요일끼리 묶어서 스케줄 아이템 생성
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
                        // 만약 운영 시간이 아예 없으면 기본값 하나 추가
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
                    // 로드 실패 시 에러 처리 (필요 시 Toast 등)
                    checkSaveButtonEnabled()
                }
        }
    }

    private fun mapIndexToDayOfWeek(index: Int): String {
        return when (index) {
            0 -> "SUNDAY"
            1 -> "MONDAY"
            2 -> "TUESDAY"
            3 -> "WEDNESDAY"
            4 -> "THURSDAY"
            5 -> "FRIDAY"
            6 -> "SATURDAY"
            else -> "MONDAY"
        }
    }

    private fun mapDayStringToInt(day: String): Int {
        return when (day.uppercase()) {
            "SUNDAY" -> 0
            "MONDAY" -> 1
            "TUESDAY" -> 2
            "WEDNESDAY" -> 3
            "THURSDAY" -> 4
            "FRIDAY" -> 5
            "SATURDAY" -> 6
            else -> 1
        }
    }

    private fun parseTime(timeStr: String): Pair<Int, Int> {
        return try {
            val parts = timeStr.split(":")
            val h = parts[0].toInt()
            val m = parts[1].toInt()
            h to m
        } catch (e: Exception) {
            0 to 0
        }
    }

    // --- Actions ---
    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun onAction(action: StoreEditAction) {
        when (action) {
            // [운영 정보 관련 액션 - Step4 이식]
            is StoreEditAction.ToggleRegularHolidayType -> {
                _uiState.update {
                    val newType = if (it.regularHolidayType == action.type) 0 else action.type
                    it.copy(regularHolidayType = newType)
                }
            }
            is StoreEditAction.SetWeeklyDialogVisible -> _uiState.update { it.copy(showWeeklyDayDialog = action.visible) }
            is StoreEditAction.SetMonthlyWeekDialogVisible -> _uiState.update { it.copy(showMonthlyWeekDialog = action.visible) }
            is StoreEditAction.SetMonthlyDayDialogVisible -> _uiState.update { it.copy(showMonthlyDayDialog = action.visible) }
            is StoreEditAction.SetTempHolidayDatePickerVisible -> _uiState.update { it.copy(showTempHolidayDatePicker = action.visible) }

            is StoreEditAction.UpdateWeeklyHolidays -> _uiState.update { it.copy(weeklyHolidayDays = action.days, showWeeklyDayDialog = false) }
            is StoreEditAction.UpdateMonthlyWeeks -> _uiState.update { it.copy(monthlyHolidayWeeks = action.weeks, showMonthlyWeekDialog = false) }
            is StoreEditAction.UpdateMonthlyDays -> _uiState.update { it.copy(monthlyHolidayDays = action.days, showMonthlyDayDialog = false) }

            StoreEditAction.ToggleTempHoliday -> _uiState.update { it.copy(isTempHolidayEnabled = !it.isTempHolidayEnabled) }
            is StoreEditAction.UpdateTempHolidayRange -> {
                _uiState.update {
                    it.copy(tempHolidayStart = action.start, tempHolidayEnd = action.end, showTempHolidayDatePicker = false)
                }
            }

            StoreEditAction.AddOperatingSchedule -> {
                val newId = (_uiState.value.operatingSchedules.maxOfOrNull { it.id } ?: 0) + 1
                val newItem = OperatingScheduleItem(newId, startHour = 18, startMin = 0, endHour = 0, endMin = 0)
                _uiState.update { it.copy(operatingSchedules = it.operatingSchedules + newItem) }
            }
            is StoreEditAction.UpdateOperatingDays -> updateOperatingScheduleDays(action.id, action.dayIdx)
            is StoreEditAction.UpdateOperatingTime -> updateOperatingScheduleTime(action.id, action.startHour, action.startMin, action.endHour, action.endMin)
            is StoreEditAction.RemoveOperatingSchedule -> {
                _uiState.update { it.copy(operatingSchedules = it.operatingSchedules.filter { item -> item.id != action.id }) }
            }
        }
        // 액션 발생 시마다 유효성 검사 (저장 버튼 활성화용)
        checkSaveButtonEnabled()
    }

    // --- Logic Functions (Step4 Logic 이식) ---

    private fun updateOperatingScheduleDays(id: Long, dayIdx: Int) {
        val currentSchedules = _uiState.value.operatingSchedules
        val targetItem = currentSchedules.find { it.id == id } ?: return
        val isOccupiedByOther = currentSchedules.any { item ->
            item.id != id && item.selectedDays.contains(dayIdx)
        }

        if (isOccupiedByOther && !targetItem.selectedDays.contains(dayIdx)) {
            viewModelScope.launch { _event.emit(StoreEditMainEvent.ShowToast("이미 설정된 요일입니다.")) }
            return
        }

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

    private fun updateOperatingScheduleTime(id: Long, sH: Int, sM: Int, eH: Int, eM: Int) {
        _uiState.update { state ->
            val updatedList = state.operatingSchedules.map { item ->
                if (item.id == id) item.copy(startHour = sH, startMin = sM, endHour = eH, endMin = eM) else item
            }
            state.copy(operatingSchedules = updatedList)
        }
    }

    // ★ [핵심] 저장 버튼 활성화 조건 체크 (Step4의 Next버튼 로직과 동일)
    private fun checkSaveButtonEnabled() {
        val state = _uiState.value
        // 조건: 운영 스케줄이 하나 이상 존재하고, 모든 스케줄에 요일이 하나 이상 선택되어야 함
        val isValid = state.operatingSchedules.isNotEmpty() &&
                state.operatingSchedules.all { it.selectedDays.isNotEmpty() }

        _uiState.update { it.copy(isSaveButtonEnabled = isValid) }
    }

    // ★ [수정] 저장 버튼 클릭 핸들러
    fun onSaveClick() {
        viewModelScope.launch {
            // 추후 탭별/일괄 저장을 위한 확장 구조
            val results = mutableListOf<Result<Unit>>()
            val currentTab = _uiState.value.selectedTabIndex

            // 1. 운영 정보 탭일 경우 (또는 전체 저장 시)
            if (currentTab == 0) {
                val result = saveOperationInfo()
                results.add(result)
            }

            // 2. 메뉴 탭일 경우 (추후 구현)
            // if (currentTab == 1) { results.add(saveMenuInfo()) }

            // 3. 사진 탭일 경우 (추후 구현)
            // if (currentTab == 2) { results.add(savePhotoInfo()) }

            // 결과 처리
            if (results.all { it.isSuccess }) {
                _event.emit(StoreEditMainEvent.ShowToast("저장되었습니다."))
                _event.emit(StoreEditMainEvent.NavigateBack)
            } else {
                // 실패 시 에러 메시지 처리 (첫 번째 에러 메시지 사용 등)
                val error = results.firstOrNull { it.isFailure }?.exceptionOrNull()?.message
                _event.emit(StoreEditMainEvent.ShowToast(error ?: "저장에 실패했습니다."))
            }
        }
    }

    // ★ [신규] 운영 정보 저장 로직 (UI State -> Domain Model 매핑 및 API 호출)
    private suspend fun saveOperationInfo(): Result<Unit> {
        val state = _uiState.value

        // 1. 정기 휴무일 매핑
        val regularHolidays = when (state.regularHolidayType) {
            1 -> { // 매주 (weekInfo = 0)
                state.weeklyHolidayDays.map { dayIdx ->
                    RegularHoliday(dayOfWeek = mapIndexToDayOfWeek(dayIdx), weekInfo = 0)
                }
            }
            2 -> { // 매월 (weekInfo = 1~5, 10)
                // 주차(Weeks)와 요일(Days)의 모든 조합 생성
                state.monthlyHolidayWeeks.flatMap { week ->
                    state.monthlyHolidayDays.map { dayIdx ->
                        RegularHoliday(dayOfWeek = mapIndexToDayOfWeek(dayIdx), weekInfo = week)
                    }
                }
            }
            else -> emptyList() // 없음
        }

        // 2. 임시 휴무일 매핑
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

        // 3. 운영 시간 매핑
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
        return updateStoreOperationInfoUseCase(regularHolidays, tempHolidays, openingHours)
    }
}

sealed interface StoreEditMainEvent {
    data object NavigateBack : StoreEditMainEvent
    data class ShowToast(val message: String) : StoreEditMainEvent
}

// Action 정의 (Step4 Action들을 가져옴)
sealed interface StoreEditAction {
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
}