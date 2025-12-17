package com.gmg.seatnow.presentation.owner.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerSignUpViewModel @Inject constructor() : ViewModel() {

    // 1. UI 상태 (입력값 + 현재 단계)
    private val _uiState = MutableStateFlow(OwnerSignUpUiState())
    val uiState: StateFlow<OwnerSignUpUiState> = _uiState.asStateFlow()

    // 2. 단발성 이벤트 (화면 이동 등)
    private val _event = MutableSharedFlow<SignUpEvent>()
    val event: SharedFlow<SignUpEvent> = _event.asSharedFlow()

    // 3. 사용자 동작 처리
    fun onAction(action: SignUpAction) {
        when (action) {
            // 입력값 업데이트
            is SignUpAction.UpdateEmail -> _uiState.update { it.copy(email = action.email) }
            is SignUpAction.UpdateAuthCode -> _uiState.update { it.copy(authCode = action.code) }
            is SignUpAction.UpdatePassword -> _uiState.update { it.copy(password = action.password) }
            is SignUpAction.UpdatePasswordCheck -> _uiState.update { it.copy(passwordCheck = action.check) }
            is SignUpAction.UpdatePhone -> _uiState.update { it.copy(phone = action.phone) }

            // 버튼 클릭 이벤트
            is SignUpAction.OnNextClick -> handleNextStep()
            is SignUpAction.OnBackClick -> handleBackStep()
        }
    }

    private fun handleNextStep() {
        val currentStep = _uiState.value.currentStep
        val nextOrdinal = currentStep.ordinal + 1

        if (nextOrdinal < SignUpStep.entries.size) {
            // 다음 단계로 이동
            val nextStep = SignUpStep.entries[nextOrdinal]
            _uiState.update { it.copy(currentStep = nextStep) }
        } else {
            // 마지막 단계(완료)라면 홈으로 이동 이벤트 발생
            viewModelScope.launch {
                _event.emit(SignUpEvent.NavigateToHome)
            }
        }
    }

    private fun handleBackStep() {
        val currentStep = _uiState.value.currentStep
        val prevOrdinal = currentStep.ordinal - 1

        if (prevOrdinal >= 0) {
            // 이전 단계로 이동
            _uiState.update { it.copy(currentStep = SignUpStep.entries[prevOrdinal]) }
        } else {
            // 첫 단계에서 뒤로가기 시 화면 종료 이벤트 발생
            viewModelScope.launch {
                _event.emit(SignUpEvent.NavigateBack)
            }
        }
    }
}

// --- 관련 데이터 클래스들 (같은 파일 하단 혹은 별도 파일) ---

data class OwnerSignUpUiState(
    val currentStep: SignUpStep = SignUpStep.STEP_1_BASIC,
    val email: String = "",
    val authCode: String = "",
    val password: String = "",
    val passwordCheck: String = "",
    val phone: String = "",
    // 필요 시 사업자 번호, 가게 이름 등 필드 추가
    val businessNumber: String = "",
    val storeName: String = ""
)

sealed interface SignUpAction {
    data class UpdateEmail(val email: String) : SignUpAction
    data class UpdateAuthCode(val code: String) : SignUpAction
    data class UpdatePassword(val password: String) : SignUpAction
    data class UpdatePasswordCheck(val check: String) : SignUpAction
    data class UpdatePhone(val phone: String) : SignUpAction

    object OnNextClick : SignUpAction
    object OnBackClick : SignUpAction
}

sealed interface SignUpEvent {
    object NavigateBack : SignUpEvent
    object NavigateToHome : SignUpEvent
}