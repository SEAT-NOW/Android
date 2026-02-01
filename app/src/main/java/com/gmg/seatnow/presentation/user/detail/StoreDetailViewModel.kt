package com.gmg.seatnow.presentation.user.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.MenuItemUiModel
import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.domain.repository.MapRepository
import com.gmg.seatnow.domain.usecase.store.GetStoreDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreDetailViewModel @Inject constructor(
    private val getStoreDetailUseCase: GetStoreDetailUseCase,
    private val mapRepository: MapRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // ★ [크래시 해결] Long 타입을 String으로 강제 캐스팅하지 않고, 타입 체크 후 변환
    private val storeId: Long = when (val id = savedStateHandle.get<Any>("storeId")) {
        is Long -> id
        is Int -> id.toLong()
        is String -> id.toLongOrNull() ?: -1L
        else -> -1L
    }

    private val _storeDetailState = MutableStateFlow<StoreDetail?>(null)
    val storeDetailState: StateFlow<StoreDetail?> = _storeDetailState.asStateFlow()

    private val _menuListState = MutableStateFlow<List<MenuCategoryUiModel>>(emptyList())
    val menuListState: StateFlow<List<MenuCategoryUiModel>> = _menuListState.asStateFlow()

    private val _eventChannel = Channel<UiEvent>(Channel.BUFFERED)
    val eventFlow = _eventChannel.receiveAsFlow()

    init {
        loadStoreDetail()
    }

    private fun loadStoreDetail() {
        if (storeId == -1L) {
            sendEvent(UiEvent.ShowToast("잘못된 접근입니다(ID 오류). 테스트 모드로 실행합니다."))
            setMockData() // ★ ID가 꼬여도 화면 띄움
            return
        }

        viewModelScope.launch {
            getStoreDetailUseCase(storeId)
                .onSuccess { (detail, menus) ->
                    _storeDetailState.value = detail
                    _menuListState.value = menus
                }
                .onFailure { e ->
                    e.printStackTrace()
                    // ★ [수정] 실패 시 뒤로가기 대신 테스트 데이터 표시
                    sendEvent(UiEvent.ShowToast("정보를 불러오지 못했습니다. 테스트 데이터를 표시합니다."))
                    setMockData()
                }
        }
    }

    // ★ 테스트용 가짜 데이터 생성
    private fun setMockData() {
        val mockDetail = StoreDetail(
            id = storeId,
            name = "[테스트] 데이터 로드 실패",
            images = emptyList(),
            operationStatus = "정보 없음",
            storePhone = "010-0000-0000",
            availableSeatCount = 0,
            totalSeatCount = 0,
            status = StoreStatus.NORMAL,
            universityInfo = "서버 연결 확인 필요",
            address = "네트워크 상태를 확인해주세요.",
            openHours = "- ~ -",
            closedDays = "-",
            isKept = false
        )

        val mockMenus = listOf(
            MenuCategoryUiModel(
                categoryName = "안내",
                menuItems = listOf(
                    MenuItemUiModel(0, "메뉴 정보를 불러올 수 없습니다.", 0, "", false, false)
                )
            )
        )

        _storeDetailState.value = mockDetail
        _menuListState.value = mockMenus
    }

    fun toggleStoreKeep() {
        val currentDetail = _storeDetailState.value ?: return
        val newKeptState = !currentDetail.isKept

        viewModelScope.launch {
            _storeDetailState.value = currentDetail.copy(isKept = newKeptState)
            mapRepository.toggleStoreKeep(storeId, newKeptState)
                .onSuccess { }
                .onFailure { e ->
                    _storeDetailState.value = currentDetail.copy(isKept = !newKeptState)
                    sendEvent(UiEvent.ShowToast("찜하기 실패 (테스트 모드)"))
                }
        }
    }

    fun onLikeClicked(menuId: Long, isLiked: Boolean) {
        val currentMenus = _menuListState.value
        _menuListState.value = currentMenus.map { category ->
            category.copy(
                menuItems = category.menuItems.map { item ->
                    if (item.id == menuId) item.copy(isLiked = isLiked) else item
                }
            )
        }
    }

    private fun sendEvent(event: UiEvent) {
        viewModelScope.launch { _eventChannel.send(event) }
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        data object NavigateBack : UiEvent()
    }
}

//package com.gmg.seatnow.presentation.user.detail
//
//import androidx.lifecycle.SavedStateHandle
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.gmg.seatnow.domain.model.MenuCategoryUiModel
//import com.gmg.seatnow.domain.model.StoreDetail
//import com.gmg.seatnow.domain.repository.MapRepository
//import com.gmg.seatnow.domain.usecase.store.GetStoreDetailUseCase
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.channels.Channel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.receiveAsFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class StoreDetailViewModel @Inject constructor(
//    private val getStoreDetailUseCase: GetStoreDetailUseCase,
//    private val mapRepository: MapRepository,
//    savedStateHandle: SavedStateHandle
//) : ViewModel() {
//
//    // ★ [문제 해결 1] storeId 파싱 강화 (API 호출 실패 원인 제거)
//    // Navigation 인자가 Int(7)로 올 수도, String("7")로 올 수도 있음을 모두 방어
//    private val storeId: Long by lazy {
//        val rawId = savedStateHandle.get<Any>("storeId")
//        when (rawId) {
//            is Long -> rawId
//            is Int -> rawId.toLong() // 숫자로 넘어온 경우
//            is String -> rawId.toLongOrNull() ?: -1L // 문자로 넘어온 경우
//            else -> -1L
//        }
//    }
//
//    private val _storeDetailState = MutableStateFlow<StoreDetail?>(null)
//    val storeDetailState: StateFlow<StoreDetail?> = _storeDetailState.asStateFlow()
//
//    private val _menuListState = MutableStateFlow<List<MenuCategoryUiModel>>(emptyList())
//    val menuListState: StateFlow<List<MenuCategoryUiModel>> = _menuListState.asStateFlow()
//
//    // ★ [문제 해결 2] Buffered Channel 사용 (무한 로딩 해결)
//    // 화면이 그려지기 전에 발생한 이벤트(에러 등)를 저장해둠
//    private val _eventChannel = Channel<UiEvent>(Channel.BUFFERED)
//    val eventFlow = _eventChannel.receiveAsFlow()
//
//    init {
//        loadStoreDetail()
//    }
//
//    private fun loadStoreDetail() {
//        if (storeId == -1L) {
//            sendEvent(UiEvent.ShowToast("잘못된 접근입니다."))
//            sendEvent(UiEvent.NavigateBack)
//            return
//        }
//
//        viewModelScope.launch {
//            //
//            getStoreDetailUseCase(storeId)
//                .onSuccess { (detail, menus) ->
//                    _storeDetailState.value = detail
//                    _menuListState.value = menus
//                }
//                .onFailure { e ->
//                    e.printStackTrace()
//                    sendEvent(UiEvent.ShowToast("매장 정보를 불러오지 못했습니다."))
//                    sendEvent(UiEvent.NavigateBack)
//                }
//        }
//    }
//
//    fun toggleStoreKeep() {
//        val currentDetail = _storeDetailState.value ?: return
//        val newKeptState = !currentDetail.isKept
//
//        viewModelScope.launch {
//            // 낙관적 업데이트
//            _storeDetailState.value = currentDetail.copy(isKept = newKeptState)
//
//            mapRepository.toggleStoreKeep(storeId, newKeptState)
//                .onSuccess { }
//                .onFailure { e ->
//                    // 롤백
//                    _storeDetailState.value = currentDetail.copy(isKept = !newKeptState)
//
//                    val msg = when (e.message) {
//                        "Unauthorized" -> "로그인이 필요한 서비스입니다."
//                        "Forbidden" -> "이용 권한이 없습니다."
//                        else -> "잠시 후 다시 시도해주세요."
//                    }
//                    sendEvent(UiEvent.ShowToast(msg))
//                }
//        }
//    }
//
//    fun onLikeClicked(menuId: Long, isLiked: Boolean) {
//        val currentMenus = _menuListState.value
//        _menuListState.value = currentMenus.map { category ->
//            category.copy(
//                menuItems = category.menuItems.map { item ->
//                    if (item.id == menuId) item.copy(isLiked = isLiked) else item
//                }
//            )
//        }
//    }
//
//    private fun sendEvent(event: UiEvent) {
//        viewModelScope.launch {
//            _eventChannel.send(event)
//        }
//    }
//
//    sealed class UiEvent {
//        data class ShowToast(val message: String) : UiEvent()
//        data object NavigateBack : UiEvent()
//    }
//}