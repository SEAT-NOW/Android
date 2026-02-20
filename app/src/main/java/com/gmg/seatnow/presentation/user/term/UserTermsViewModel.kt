package com.gmg.seatnow.presentation.user.term

import androidx.lifecycle.ViewModel
import com.gmg.seatnow.data.local.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class UserTermsViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserTermsUiState())
    val uiState: StateFlow<UserTermsUiState> = _uiState.asStateFlow()

    // 어떤 모드로 들어왔는지에 따라 다르게 저장
    fun saveTermsAgreement(isGuest: Boolean) {
        if (isGuest) {
            authManager.setGuestTermsAgreed(true)
        } else {
            authManager.setKakaoTermsAgreed(true)
        }
    }

    // 약관 전체 동의 토글
    fun toggleAll(isChecked: Boolean) {
        _uiState.update {
            it.copy(
                isAllChecked = isChecked,
                isAgeChecked = isChecked,
                isServiceChecked = isChecked,
                isPrivacyCollectChecked = isChecked,
                isPrivacyProvideChecked = isChecked,
                isLocationChecked = isChecked
            )
        }
    }

    // 개별 약관 토글
    fun toggleTerm(type: UserTermType) {
        _uiState.update { state ->
            val newState = when(type) {
                UserTermType.AGE -> state.copy(isAgeChecked = !state.isAgeChecked)
                UserTermType.SERVICE -> state.copy(isServiceChecked = !state.isServiceChecked)
                UserTermType.PRIVACY_COLLECT -> state.copy(isPrivacyCollectChecked = !state.isPrivacyCollectChecked)
                UserTermType.PRIVACY_PROVIDE -> state.copy(isPrivacyProvideChecked = !state.isPrivacyProvideChecked) // 오타 방지
                UserTermType.LOCATION -> state.copy(isLocationChecked = !state.isLocationChecked)
            }
            // 전체 동의 여부 재계산
            val allChecked = newState.isAgeChecked && newState.isServiceChecked &&
                    newState.isPrivacyCollectChecked && newState.isPrivacyProvideChecked &&
                    newState.isLocationChecked
            newState.copy(isAllChecked = allChecked)
        }
    }

    // 상세 약관 열기
    fun openDetail(type: UserTermType) {
        _uiState.update { it.copy(openedTermType = type) }
    }

    // 상세 약관 닫기
    fun closeDetail() {
        _uiState.update { it.copy(openedTermType = null) }
    }
}

// 약관 타입 정의
enum class UserTermType(val title: String) {
    AGE("[필수] 만 14세 이상"),
    SERVICE("[필수] 이용약관 동의"),
    PRIVACY_COLLECT("[필수] 개인정보 수집이용 동의"),
    PRIVACY_PROVIDE("[필수] 개인정보 처리방침 동의"),
    LOCATION("[필수] 위치기반 서비스 이용약관 동의")
}

// UI 상태
data class UserTermsUiState(
    val isAllChecked: Boolean = false,
    val isAgeChecked: Boolean = false,
    val isServiceChecked: Boolean = false,
    val isPrivacyCollectChecked: Boolean = false,
    val isPrivacyProvideChecked: Boolean = false, // 변수명 통일 (Verified -> Checked)
    val isLocationChecked: Boolean = false,
    val openedTermType: UserTermType? = null // 상세 화면이 열려있는지 여부
) {
    // 모두 체크되었는지 확인
    val isNextEnabled: Boolean
        get() = isAgeChecked && isServiceChecked && isPrivacyCollectChecked &&
                isPrivacyProvideChecked && isLocationChecked
}