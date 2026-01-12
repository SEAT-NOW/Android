package com.gmg.seatnow.domain.usecase.auth

import com.gmg.seatnow.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyPhoneAuthCodeUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(phoneNumber: String, code: String): Result<Unit> {
        // 하이픈 제거 등 전처리 (필요 시)
        val formattedPhone = phoneNumber.replace("-", "").trim()
        return repository.verifyPhoneAuthCode(formattedPhone, code)
    }
}