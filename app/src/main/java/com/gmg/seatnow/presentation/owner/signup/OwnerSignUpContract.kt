package com.gmg.seatnow.presentation.owner.signup

import android.net.Uri
import com.gmg.seatnow.domain.model.OperatingScheduleItem
import com.gmg.seatnow.domain.model.SpaceItem
import com.gmg.seatnow.domain.model.StoreSearchResult

enum class TermType(val title: String) {
    AGE("[필수] 만 14세 이상"),
    SERVICE("[필수] 이용약관 동의"),
    PRIVACY_COLLECT("[필수] 개인정보 수집이용 동의"),
    PRIVACY_PROVIDE("[필수] 개인정보 처리방침 동의")
}

// ----------------------------------------------------
// [Phase 3 적용] 단계별로 상태를 묶어준 작은 바구니들
// ----------------------------------------------------
data class BasicState(
    val isAllTermsAgreed: Boolean = false,
    val isAgeVerified: Boolean = false,
    val isServiceVerified: Boolean = false,
    val isPrivacyCollectVerified: Boolean = false,
    val isPrivacyProvideVerified: Boolean = false,
    val openedTermType: TermType? = null,

    val email: String = "",
    val emailError: String? = null,
    val isEmailCodeSent: Boolean = false,
    val isEmailVerified: Boolean = false,
    val emailVerifiedError: String? = null,
    val isEmailVerificationAttempted: Boolean = false,
    val emailTimerText: String? = null,
    val isEmailTimerExpired: Boolean = false,
    val authCode: String = "",

    val password: String = "",
    val passwordError: String? = null,
    val passwordCheck: String = "",
    val passwordCheckError: String? = null,

    val phone: String = "",
    val phoneError: String? = null,
    val isPhoneCodeSent: Boolean = false,
    val isPhoneVerified: Boolean = false,
    val isPhoneVerificationAttempted: Boolean = false,
    val phoneVerifiedError: String? = null,
    val phoneTimerText: String? = null,
    val isPhoneTimerExpired: Boolean = false,
    val phoneAuthCode: String = ""
)

data class BusinessState(
    val repName: String = "",
    val businessNumber: String = "",
    val isBusinessNumVerified: Boolean = false,
    val businessNumberError: String? = null,
    val storeName: String = "",
    val mainAddress: String = "",
    val selectedLatitude: Double = 0.0,
    val selectedLongitude: Double = 0.0,
    val nearbyUniv: String = "",
    val nearbyUnivList: List<String> = emptyList(),
    val isNearbyUnivEnabled: Boolean = true,
    val nearbyUnivError: String? = null,
    val storeContact: String = "",
    val licenseFileName: String? = null,
    val licenseImageUrl: String? = null,
    val isStoreSearchVisible: Boolean = false,
    val storeSearchResults: List<StoreSearchResult> = emptyList()
)

data class StoreState(
    val spaceList: List<SpaceItem> = emptyList(),
    val selectedSpaceId: Long? = null
)

data class OperationState(
    val regularHolidayType: Int = 0,
    val weeklyHolidayDays: Set<Int> = setOf(0), // 기본값 유지
    val monthlyHolidayWeeks: Set<Int> = setOf(2, 4), // 기본값 유지
    val monthlyHolidayDays: Set<Int> = setOf(0), // 기본값 유지
    val isTempHolidayEnabled: Boolean = false,
    val tempHolidayStart: String = "",
    val tempHolidayEnd: String = "",
    val operatingSchedules: List<OperatingScheduleItem> = listOf(
        OperatingScheduleItem(id = 0, startHour = 18, startMin = 0, endHour = 0, endMin = 0)
    ),
    val showWeeklyDayDialog: Boolean = false,
    val showMonthlyWeekDialog: Boolean = false,
    val showMonthlyDayDialog: Boolean = false,
    val showTempHolidayDatePicker: Boolean = false
)

data class PhotoState(
    val storePhotoList: List<Uri> = emptyList(),
    val representativePhotoUri: Uri? = null
)

data class OwnerSignUpUiState(
    val currentStep: SignUpStep = SignUpStep.STEP_1_BASIC,
    val isNextButtonEnabled: Boolean = false,
    val basic: BasicState = BasicState(),
    val business: BusinessState = BusinessState(),
    val store: StoreState = StoreState(),
    val operation: OperationState = OperationState(),
    val photo: PhotoState = PhotoState()
)

sealed interface SignUpAction {
    // Step 1
    data class UpdateEmail(val email: String) : SignUpAction
    data class UpdateAuthCode(val code: String) : SignUpAction
    data class UpdatePassword(val password: String) : SignUpAction
    data class UpdatePasswordCheck(val check: String) : SignUpAction
    data class UpdatePhone(val phone: String) : SignUpAction
    data class UpdatePhoneAuthCode(val code: String) : SignUpAction
    data class ToggleAllTerms(val isChecked: Boolean) : SignUpAction
    data class ToggleTerm(val termType: TermType) : SignUpAction
    data class OpenTermDetail(val termType: TermType) : SignUpAction
    object CloseTermDetail : SignUpAction
    object RequestEmailCode : SignUpAction
    object VerifyEmailCode : SignUpAction
    object RequestPhoneCode : SignUpAction
    object VerifyPhoneCode : SignUpAction

    // Step 2
    data class UpdateRepName(val name: String) : SignUpAction
    data class UpdateBusinessNum(val num: String) : SignUpAction
    object VerifyBusinessNum : SignUpAction
    object OpenStoreSearch : SignUpAction
    object CloseStoreSearch : SignUpAction
    data class SearchStoreQuery(val query: String) : SignUpAction
    data class SelectStore(val store: StoreSearchResult) : SignUpAction
    data class UpdateMainAddress(val address: String) : SignUpAction
    data class UpdateStoreContact(val phone: String) : SignUpAction
    data class UploadLicenseImage(val uri: Uri, val fileName: String) : SignUpAction

    // Step 3
    object AddSpaceItemRow : SignUpAction
    data class UpdateSpaceItemInput(val id: Long, val input: String) : SignUpAction
    data class SaveSpaceItem(val id: Long) : SignUpAction
    data class SelectSpace(val id: Long) : SignUpAction
    data class RemoveSpace(val id: Long) : SignUpAction
    data class EditSpace(val id: Long) : SignUpAction

    object AddTableItemRow : SignUpAction
    data class UpdateTableItemN(val tableId: Long, val value: String) : SignUpAction
    data class UpdateTableItemM(val tableId: Long, val value: String) : SignUpAction
    data class RemoveTableItemRow(val tableId: Long) : SignUpAction

    // Step 4
    data class ToggleRegularHolidayType(val type: Int) : SignUpAction
    data class SetWeeklyDialogVisible(val visible: Boolean) : SignUpAction
    data class SetMonthlyWeekDialogVisible(val visible: Boolean) : SignUpAction
    data class SetMonthlyDayDialogVisible(val visible: Boolean) : SignUpAction
    data class SetTempHolidayDatePickerVisible(val visible: Boolean) : SignUpAction
    data class UpdateWeeklyHolidays(val days: Set<Int>) : SignUpAction
    data class UpdateMonthlyWeeks(val weeks: Set<Int>) : SignUpAction
    data class UpdateMonthlyDays(val days: Set<Int>) : SignUpAction

    object ToggleTempHoliday : SignUpAction
    data class UpdateTempHolidayRange(val start: String, val end: String) : SignUpAction

    object AddOperatingSchedule : SignUpAction
    data class UpdateOperatingDays(val id: Long, val dayIdx: Int) : SignUpAction
    data class UpdateOperatingTime(val id: Long, val startHour: Int, val startMin: Int, val endHour: Int, val endMin: Int) : SignUpAction
    data class RemoveOperatingSchedule(val id: Long) : SignUpAction

    // Step 5
    data class AddStorePhotos(val uris: List<Uri>) : SignUpAction
    data class RemoveStorePhoto(val uri: Uri) : SignUpAction
    data class SetRepresentativePhoto(val uri: Uri) : SignUpAction

    object OnNextClick : SignUpAction
    object OnBackClick : SignUpAction
}

sealed interface SignUpEvent {
    object NavigateBack : SignUpEvent
    object NavigateToHome : SignUpEvent
    data class ShowToast(val message: String) : SignUpEvent
}