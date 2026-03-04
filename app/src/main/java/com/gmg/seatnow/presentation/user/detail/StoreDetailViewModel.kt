package com.gmg.seatnow.presentation.user.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.StoreDetail
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val storeId: Long = when (val id = savedStateHandle.get<Any>("storeId")) {
        is Long -> id
        is Int -> id.toLong()
        is String -> id.toLongOrNull() ?: -1L
        else -> -1L
    }

    private val _uiState = MutableStateFlow(StoreDetailUiState())
    val uiState: StateFlow<StoreDetailUiState> = _uiState.asStateFlow()

    private val _eventChannel = Channel<StoreDetailEvent>(Channel.BUFFERED)
    val eventFlow = _eventChannel.receiveAsFlow()

    init {
        loadStoreDetail()
    }

    private fun loadStoreDetail() {
        if (storeId == -1L) {
            sendEvent(StoreDetailEvent.ShowToast("잘못된 접근입니다."))
            return
        }

        viewModelScope.launch {
            getStoreDetailUseCase(storeId)
                .onSuccess { (detail, menus) ->
                    _uiState.update { it.copy(storeDetail = detail, menuCategories = menus, isLoading = false) }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                    sendEvent(StoreDetailEvent.ShowToast("가게 정보를 불러오는데 실패했습니다."))
                }
        }
    }

    fun onAction(action: StoreDetailAction) {
        when (action) {
            is StoreDetailAction.OnKeepClicked -> onKeepClicked(action.storeId, action.newKeptState)
            is StoreDetailAction.OnLikeClicked -> onLikeClicked(action.menuId)
            is StoreDetailAction.OnBackClick -> sendEvent(StoreDetailEvent.NavigateBack)
        }
    }

    private fun onKeepClicked(id: Long, newKeptState: Boolean) {
        val currentDetail = _uiState.value.storeDetail ?: return

        // 2. 일반 회원 (API 호출)
        viewModelScope.launch {
            // 낙관적 업데이트
            _uiState.update { it.copy(storeDetail = currentDetail.copy(isKept = newKeptState)) }

            toggleStoreKeepUseCase(id, newKeptState)
                .onSuccess {
                    // 성공 시 유지
                }
                .onFailure { e ->
                    // 실패 시 롤백
                    _uiState.update { it.copy(storeDetail = currentDetail.copy(isKept = !newKeptState)) }
                    val msg = if (e.message?.contains("Token") == true) "로그인이 만료되었습니다."
                        else if(e.message == "LOGIN_REQUIRED") ("로그인이 필요한 서비스입니다.")
                        else "오류가 발생했습니다."
                    sendEvent(StoreDetailEvent.ShowToast(msg))
                }
        }
    }

    // ★ [수정] 메뉴 좋아요 (Toggle 방식이므로 Boolean 파라미터 제거)
    private fun onLikeClicked(menuId: Long) {
        val currentCategories = _uiState.value.menuCategories
        // 메뉴 찾기
        val targetItem = currentCategories.flatMap { it.menuItems }.find { it.id == menuId } ?: return
        val currentIsLiked = targetItem.isLiked


        viewModelScope.launch {
            // 낙관적 업데이트
            updateMenuLikeStateInUi(menuId, !currentIsLiked)

            toggleMenuLikeUseCase(menuId)
                .onSuccess { }
                .onFailure { e ->
                    // 롤백
                    updateMenuLikeStateInUi(menuId, currentIsLiked)
                    val msg = if (e.message?.contains("Token") == true) "로그인이 만료되었습니다."
                        else if(e.message == "LOGIN_REQUIRED") ("로그인이 필요한 서비스입니다.")
                        else "오류가 발생했습니다."
                    sendEvent(StoreDetailEvent.ShowToast(msg))
                }
        }
    }

    // [Helper] UI 상태 업데이트용 함수 (중복 제거)
    private fun updateMenuLikeStateInUi(menuId: Long, newIsLiked: Boolean) {
        _uiState.update { state ->
            val updatedCategories = state.menuCategories.map { category ->
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
            state.copy(menuCategories = updatedCategories)
        }
    }

    private fun sendEvent(event: StoreDetailEvent) {
        viewModelScope.launch { _eventChannel.send(event) }
    }
}

