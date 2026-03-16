package com.gmg.seatnow.presentation.owner.store.mypage

import com.gmg.seatnow.domain.model.SpaceItem

// 1. 상태(State) 바구니들
data class AccountState(
    val ownerEmail: String = "",
    val ownerPhoneNumber: String = "",
    val isProfileLoaded: Boolean = false
)

data class PasswordState(
    val checkPassword: String = "",
    val checkPasswordError: String? = null,
    val newPassword: String = "",
    val newPasswordError: String? = null,
    val newPasswordCheck: String = "",
    val newPasswordCheckError: String? = null,
    val isChangePasswordButtonEnabled: Boolean = false
)

data class StoreInfoState(
    val isStoreLoaded: Boolean = false,
    val representativeName: String = "",
    val businessNumber: String = "",
    val storeName: String = "",
    val storeAddress: String = "",
    val universityName: String = "",
    val licenseFileName: String = "",
    val storeContact: String = "",
    val editStoreContact: String = "",
    val editStoreContactError: String? = null,
    val isStoreContactUpdateSuccess: Boolean = false
)

data class SeatConfigState(
    val spaceList: List<SpaceItem> = emptyList(),
    val selectedSpaceId: Long = 0,
    val isSeatConfigValid: Boolean = false
)

// 통합 UI State
data class MyPageUiState(
    val isLoading: Boolean = false, // 로딩은 최상단 유지
    val account: AccountState = AccountState(),
    val password: PasswordState = PasswordState(),
    val storeInfo: StoreInfoState = StoreInfoState(),
    val seatConfig: SeatConfigState = SeatConfigState()
)

// 2. 이벤트 (Event)
sealed interface MyPageEvent {
    data object NavigateToLogin : MyPageEvent
    data object NavigateToAccountInfo : MyPageEvent
    data object NavigateToEditAccount : MyPageEvent
    data object NavigateToEditSeatConfig : MyPageEvent
    data object NavigateToCheckPassword : MyPageEvent
    data object NavigateToChangePassword : MyPageEvent
    data object NavigateBack : MyPageEvent
    data class ShowToast(val message: String) : MyPageEvent
    data object NavigateToEditStoreInfo : MyPageEvent
    data object NavigateToEditStoreContact : MyPageEvent
}

// 3. 액션 (Action)
sealed interface MyPageAction {
    data object OnLogoutClick : MyPageAction
    data object OnAccountInfoClick : MyPageAction
    data object OnEditAccountInfoClick : MyPageAction
    data object OnEditSeatConfigClick : MyPageAction
    data object OnCheckPasswordClick : MyPageAction
    data class UpdateCheckPassword(val password: String) : MyPageAction
    data object OnCheckPasswordNextClick : MyPageAction
    data class UpdateNewPassword(val password: String) : MyPageAction
    data class UpdateNewPasswordCheck(val check: String) : MyPageAction
    data object OnChangePasswordClick : MyPageAction
    data object OnEditStoreInfoClick : MyPageAction
    data object OnStoreContactClick : MyPageAction
    data class UpdateStoreContactInput(val input: String) : MyPageAction
    data object OnStoreContactConfirmClick : MyPageAction
    data class SelectSpace(val id: Long) : MyPageAction
    data class EditSpace(val id: Long) : MyPageAction
    data class UpdateSpaceItemInput(val id: Long, val input: String) : MyPageAction
    data class SaveSpaceItem(val id: Long) : MyPageAction
    data object AddSpaceItemRow : MyPageAction
    data class RemoveSpace(val id: Long) : MyPageAction
    data object AddTableItemRow : MyPageAction
    data class RemoveTableItemRow(val tableId: Long) : MyPageAction
    data class UpdateTableItemN(val tableId: Long, val n: String) : MyPageAction
    data class UpdateTableItemM(val tableId: Long, val m: String) : MyPageAction
    data object OnSaveSeatConfigClick : MyPageAction
}