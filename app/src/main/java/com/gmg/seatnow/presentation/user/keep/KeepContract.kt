package com.gmg.seatnow.presentation.user.keep

data class KeepUiState(
    val keepList: List<KeepStoreUiModel> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface KeepAction {
    object FetchKeepList : KeepAction
    data class ToggleKeep(val item: KeepStoreUiModel) : KeepAction
}

sealed interface KeepEvent {
    // 필요한 경우 에러 토스트나 네비게이션 이벤트 추가
}
