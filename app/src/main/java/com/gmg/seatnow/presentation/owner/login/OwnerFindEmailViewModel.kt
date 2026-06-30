package com.gmg.seatnow.presentation.owner.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmg.seatnow.domain.usecase.common.auth.FindEmailUseCase
import com.gmg.seatnow.domain.usecase.common.auth.RequestPhoneAuthCodeUseCase
import com.gmg.seatnow.domain.usecase.common.auth.VerifyPhoneAuthCodeUseCase
import com.gmg.seatnow.domain.usecase.common.validation.CheckTestAccountUseCase
import com.gmg.seatnow.domain.usecase.common.logic.FormatTimerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OwnerFindEmailViewModel @Inject constructor(
    private val requestPhoneAuthCodeUseCase: RequestPhoneAuthCodeUseCase,
    private val verifyPhoneAuthCodeUseCase: VerifyPhoneAuthCodeUseCase,
    private val findEmailUseCase: FindEmailUseCase,
    private val formatTimerUseCase: FormatTimerUseCase,
    private val checkTestAccountUseCase: CheckTestAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OwnerFindEmailUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<OwnerFindEmailEvent>()
    val event = _event.asSharedFlow()

    private var phoneTimerJob: Job? = null

    fun onAction(action: OwnerFindEmailAction) {
        when (action) {
            is OwnerFindEmailAction.UpdatePhone -> {
                if(action.phone.length <= 11 && action.phone.all { it.isDigit() }) {
                    _uiState.update { it.copy(phone = action.phone) }
                }
            }
            is OwnerFindEmailAction.UpdatePhoneAuthCode -> {
                if (action.code.length <= 6 && action.code.all { it.isDigit() }) {
                    _uiState.update { it.copy(phoneAuthCode = action.code) }
                }
            }
            is OwnerFindEmailAction.RequestPhoneCode -> requestPhoneCode()
            is OwnerFindEmailAction.VerifyPhoneCode -> verifyPhoneCode()
            is OwnerFindEmailAction.OnFindEmailClick -> {
                viewModelScope.launch {
                    val phone = _uiState.value.phone
                    
                    if (checkTestAccountUseCase.isTestPhone(phone)) {
                        _uiState.update { 
                            it.copy(
                                isResultScreenVisible = true,
                                findEmailResult = "test@seatnow.com",
                                findEmailError = null
                            )
                        }
                        return@launch
                    }
                    
                    findEmailUseCase(phone)
                        .onSuccess { email ->
                            _uiState.update { 
                                it.copy(
                                    isResultScreenVisible = true,
                                    findEmailResult = email,
                                    findEmailError = null
                                )
                            }
                        }
                        .onFailure { exception ->
                            _uiState.update {
                                it.copy(
                                    isResultScreenVisible = true,
                                    findEmailResult = "",
                                    findEmailError = exception.message ?: "이메일 찾기에 실패했습니다."
                                )
                            }
                        }
                }
            }
            is OwnerFindEmailAction.OnConfirmResultClick -> {
                viewModelScope.launch {
                    _event.emit(OwnerFindEmailEvent.NavigateToLogin)
                }
            }
        }
        checkNextButtonEnabled()
    }

    private fun requestPhoneCode() {
        val phone = _uiState.value.phone
        if (phone.length < 10) return

        if (checkTestAccountUseCase.isTestPhone(phone)) {
            startPhoneTimer()
            _uiState.update {
                it.copy(
                    isPhoneCodeSent = true,
                    phoneAuthCode = "",
                    isPhoneVerificationAttempted = false,
                    phoneVerifiedError = null
                )
            }
            viewModelScope.launch {
                _event.emit(OwnerFindEmailEvent.ShowToast("[TEST] 인증번호 123456을 입력하세요."))
            }
            return
        }

        viewModelScope.launch {
            requestPhoneAuthCodeUseCase(phone)
                .onSuccess {
                    startPhoneTimer()
                    _uiState.update {
                        it.copy(
                            isPhoneCodeSent = true,
                            phoneAuthCode = "",
                            isPhoneVerificationAttempted = false,
                            phoneVerifiedError = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(phoneError = exception.message ?: "인증번호 전송에 실패했습니다.") }
                }
        }
    }

    private fun verifyPhoneCode() {
        val phone = _uiState.value.phone
        val code = _uiState.value.phoneAuthCode
        _uiState.update { it.copy(isPhoneVerificationAttempted = true) }

        if (checkTestAccountUseCase.isTestPhone(phone) && code == "123456") {
            stopPhoneTimer()
            _uiState.update {
                it.copy(
                    isPhoneVerified = true,
                    phoneTimerText = null,
                    phoneVerifiedError = null
                )
            }
            checkNextButtonEnabled()
            return
        }

        stopPhoneTimer()
        viewModelScope.launch {
            verifyPhoneAuthCodeUseCase(phone, code)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isPhoneVerified = true,
                            phoneTimerText = null,
                            phoneVerifiedError = null
                        )
                    }
                    checkNextButtonEnabled()
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(phoneVerifiedError = exception.message ?: "인증 번호가 일치하지 않습니다.") }
                }
        }
    }

    private fun startPhoneTimer() {
        phoneTimerJob?.cancel()
        phoneTimerJob = viewModelScope.launch {
            var time = 180
            _uiState.update { it.copy(isPhoneTimerExpired = false) }
            while (time > 0) {
                val timeString = formatTimerUseCase(time)
                _uiState.update { it.copy(phoneTimerText = timeString) }
                delay(1000)
                time--
            }
            _uiState.update { it.copy(phoneTimerText = "0:00", isPhoneTimerExpired = true) }
        }
    }

    private fun stopPhoneTimer() {
        phoneTimerJob?.cancel()
        _uiState.update { it.copy(phoneTimerText = null) }
    }

    private fun checkNextButtonEnabled() {
        _uiState.update { state ->
            state.copy(isNextButtonEnabled = state.isPhoneVerified)
        }
    }
}
