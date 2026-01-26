package com.gmg.seatnow.presentation.user.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.model.MenuCategoryUiModel
import com.gmg.seatnow.domain.model.StoreDetail
import com.gmg.seatnow.domain.usecase.store.GetStoreDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreDetailViewModel @Inject constructor(
    private val getStoreDetailUseCase: GetStoreDetailUseCase,
    savedStateHandle: SavedStateHandle // Navigation 파라미터를 받기 위한 툴
) : ViewModel() {

    // 네비게이션 스택에서 storeId를 가져옵니다.
    private val storeId: Long = checkNotNull(savedStateHandle["storeId"])

    private val _storeDetailState = MutableStateFlow<StoreDetail?>(null)
    val storeDetailState: StateFlow<StoreDetail?> = _storeDetailState.asStateFlow()

    // 메뉴 상태 (이전 대화에서 만들었던 메뉴 상태)
    private val _menuListState = MutableStateFlow<List<MenuCategoryUiModel>>(emptyList())
    val menuListState: StateFlow<List<MenuCategoryUiModel>> = _menuListState.asStateFlow()

    init {
        // ViewModel이 생성될 때 (화면 진입 시) API 호출
        fetchStoreDetail()
    }

    private fun fetchStoreDetail() {
        viewModelScope.launch {
            try {
                // UseCase를 통해 가게 ID로 상세 정보 조회
                val result = getStoreDetailUseCase(storeId)
                _storeDetailState.value = result
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    // 좋아요 토글 이벤트 처리
    fun toggleMenuLike(menuId: Long, isLiked: Boolean) {
        viewModelScope.launch {
            // 1. API UseCase 호출 (좋아요 서버 전송)
            // toggleLikeUseCase(menuId, isLiked)

            // 2. 로컬 UI State 업데이트 (단방향 데이터 흐름 준수)
            _menuListState.value = _menuListState.value.map { category ->
                category.copy(
                    menuItems = category.menuItems.map { item ->
                        if (item.id == menuId) item.copy(isLiked = isLiked) else item
                    }
                )
            }
        }
    }
}