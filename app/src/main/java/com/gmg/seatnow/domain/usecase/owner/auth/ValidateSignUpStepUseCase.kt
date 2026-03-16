package com.gmg.seatnow.domain.usecase.owner.auth

import com.gmg.seatnow.domain.model.OperatingScheduleItem
import com.gmg.seatnow.domain.model.SpaceItem
import javax.inject.Inject

/**
 * 회원가입 단계별 "다음" 버튼 활성화 조건을 판단하는 UseCase.
 * 도메인 계층 순수성 유지를 위해 UiState 대신 각 단계에 필요한 값만 직접 받는다.
 */
class ValidateSignUpStepUseCase @Inject constructor() {

    fun validateStep1(
        isEmailVerified: Boolean,
        isPhoneVerified: Boolean,
        password: String,
        passwordError: String?,
        passwordCheck: String,
        passwordCheckError: String?,
        isAllTermsAgreed: Boolean
    ): Boolean {
        return isEmailVerified && isPhoneVerified &&
                password.isNotBlank() && passwordError == null &&
                passwordCheck.isNotBlank() && passwordCheckError == null &&
                isAllTermsAgreed
    }

    fun validateStep2(
        repName: String,
        isBusinessNumVerified: Boolean,
        storeName: String,
        mainAddress: String
    ): Boolean {
        return repName.isNotBlank() &&
                isBusinessNumVerified &&
                storeName.isNotBlank() &&
                mainAddress.isNotBlank()
    }

    fun validateStep3(spaceList: List<SpaceItem>): Boolean {
        return spaceList.isNotEmpty() && spaceList.none { it.isEditing }
    }

    fun validateStep4(operatingSchedules: List<OperatingScheduleItem>): Boolean {
        return operatingSchedules.isNotEmpty() &&
                operatingSchedules.all { it.selectedDays.isNotEmpty() }
    }
}
