package com.gmg.seatnow.presentation.user.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.MenuItemUiModel
import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.domain.repository.MapRepository
import com.gmg.seatnow.domain.usecase.store.GetStoreDetailUseCase
import com.gmg.seatnow.domain.usecase.store.ToggleMenuLikeUseCase
import com.gmg.seatnow.domain.usecase.store.ToggleStoreKeepUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreDetailViewModel @Inject constructor(
    private val getStoreDetailUseCase: GetStoreDetailUseCase,
    private val toggleMenuLikeUseCase: ToggleMenuLikeUseCase,
    private val toggleStoreKeepUseCase: ToggleStoreKeepUseCase,
    private val authManager: AuthManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

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
            sendEvent(UiEvent.ShowToast("잘못된 접근입니다."))
            return
        }

        viewModelScope.launch {
            getStoreDetailUseCase(storeId)
                .onSuccess { (detail, menus) ->
                    // ★ [테스터 모드 로직 보강]
                    // 상세 정보를 불러올 때, 이미 AuthManager의 가짜 리스트에 있는 녀석이라면
                    // 서버에서 받은 데이터(detail.isKept)를 무시하고 true로 덮어씌워야 '계속 킵 된 상태'로 보임
                    val isFakeKept = if (authManager.isTester()) {
                        authManager.getFakeKeepList().any { it.id == detail.id }
                    } else {
                        false
                    }

                    val finalDetail = if (isFakeKept) detail.copy(isKept = true) else detail

                    _storeDetailState.value = finalDetail
                    _menuListState.value = menus
                }
                .onFailure {
                    sendEvent(UiEvent.ShowToast("가게 정보를 불러오는데 실패했습니다."))
                }
        }
    }

    fun onKeepClicked(id: Long, newKeptState: Boolean) {
        val currentDetail = _storeDetailState.value ?: return

        // 1. 테스터 모드 (가짜 성공 & 메모리 저장)
        if (authManager.isTester()) {
            // UI 즉시 반영
            _storeDetailState.value = currentDetail.copy(isKept = newKeptState)

            // ★ [복구된 로직] AuthManager 메모리 리스트에 추가/삭제
            if (newKeptState) {
                authManager.addFakeKeep(currentDetail)
            } else {
                authManager.removeFakeKeep(id)
            }
            return
        }

        // 2. 게스트 모드 (차단)
        if (!authManager.hasToken()) {
            sendEvent(UiEvent.ShowToast("로그인이 필요한 서비스입니다."))
            return
        }

        // 3. 일반 회원 (API 호출)
        viewModelScope.launch {
            // 낙관적 업데이트
            _storeDetailState.value = currentDetail.copy(isKept = newKeptState)

            toggleStoreKeepUseCase(id, newKeptState)
                .onSuccess {
                    // 성공 시 유지
                }
                .onFailure { e ->
                    // 실패 시 롤백
                    _storeDetailState.value = currentDetail.copy(isKept = !newKeptState)
                    val msg = if (e.message?.contains("Token") == true) "로그인이 만료되었습니다." else "오류가 발생했습니다."
                    sendEvent(UiEvent.ShowToast(msg))
                }
        }
    }

    // ★ [수정] 메뉴 좋아요 (Toggle 방식이므로 Boolean 파라미터 제거)
    fun onLikeClicked(menuId: Long) {
        val currentCategories = _menuListState.value
        // 메뉴 찾기
        val targetItem = currentCategories.flatMap { it.menuItems }.find { it.id == menuId } ?: return
        val currentIsLiked = targetItem.isLiked

        // 1. 테스터 모드
        if (authManager.isTester()) {
            updateMenuLikeStateInUi(menuId, !currentIsLiked)
            return
        }

        // 2. 게스트 모드
        if (!authManager.hasToken()) {
            sendEvent(UiEvent.ShowToast("로그인이 필요한 서비스입니다."))
            return
        }

        // 3. 일반 회원
        viewModelScope.launch {
            // 낙관적 업데이트
            updateMenuLikeStateInUi(menuId, !currentIsLiked)

            toggleMenuLikeUseCase(menuId)
                .onSuccess { }
                .onFailure { e ->
                    // 롤백
                    updateMenuLikeStateInUi(menuId, currentIsLiked)
                    val msg = if (e.message?.contains("Token") == true) "로그인이 만료되었습니다." else "오류가 발생했습니다."
                    sendEvent(UiEvent.ShowToast(msg))
                }
        }
    }

    // [Helper] UI 상태 업데이트용 함수 (중복 제거)
    private fun updateMenuLikeStateInUi(menuId: Long, newIsLiked: Boolean) {
        _menuListState.update { categories ->
            categories.map { category ->
                // 해당 카테고리에 타겟 메뉴가 있는지 확인 후 업데이트
                category.copy(
                    menuItems = category.menuItems.map { item ->
                        if (item.id == menuId) {
                            item.copy(isLiked = newIsLiked)
                        } else {
                            item
                        }
                    }
                )
            }
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

