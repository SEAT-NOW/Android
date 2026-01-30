package com.gmg.seatnow.presentation.owner.store.mypage.storeManage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.MenuItemUiModel
import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.domain.model.StoreStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class StoreManagementViewModel @Inject constructor(
    // 필요한 UseCase 주입 (GetStoreDetailUseCase 등)
) : ViewModel() {

    private val _storeDetailState = MutableStateFlow<StoreDetail?>(null)
    val storeDetailState: StateFlow<StoreDetail?> = _storeDetailState.asStateFlow()

    private val _menuListState = MutableStateFlow<List<MenuCategoryUiModel>>(emptyList())
    val menuListState: StateFlow<List<MenuCategoryUiModel>> = _menuListState.asStateFlow()

    init {
        loadStoreData()
    }

    private fun loadStoreData() {
        // TODO: 실제로는 사장님 가게 ID를 기반으로 UseCase를 호출해야 합니다.
        // 현재는 StoreDetailViewModel과 동일한 더미 데이터를 로드합니다.
        
        // 1. StoreDetail 더미
        _storeDetailState.value = StoreDetail(
            id = 1L,
            name = "맛있는 술집 신촌본점",
            images = listOf("dummy1", "dummy2"),
            operationStatus = "영업 중",
            storePhone = "02-1234-5678",
            availableSeatCount = 12,
            totalSeatCount = 50,
            status = StoreStatus.NORMAL,
            universityInfo = "연세대학교 신촌캠퍼스 도보 5분",
            address = "서울특별시 서대문구 연세로 12길 34",
            openHours = "매일 17:00 ~ 03:00",
            closedDays = "연중무휴",
            isKept = false
        )

        // 2. Menu 더미
        _menuListState.value = listOf(
            MenuCategoryUiModel(
                categoryName = "메인 메뉴",
                menuItems = listOf(
                    MenuItemUiModel(1, "나가사키 짬뽕탕", 22000, "", true, false),
                    MenuItemUiModel(2, "모듬 사시미 (대)", 35000, "", true, true)
                )
            ),
            MenuCategoryUiModel(
                categoryName = "주류",
                menuItems = listOf(
                    MenuItemUiModel(3, "참이슬", 5000, "", false, false),
                    MenuItemUiModel(4, "테라", 5000, "", false, false)
                )
            )
        )
    }
}