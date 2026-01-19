package com.gmg.seatnow.domain.usecase.auth

import com.gmg.seatnow.domain.repository.AuthRepository
import javax.inject.Inject

// [SRP 준수] 휴대폰 인증번호 발송 전용 UseCase
class RequestPhoneAuthCodeUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(phoneNumber: String): Result<Unit> {
        // 필요하다면 여기서 휴대폰 번호 포맷팅 로직(하이픈 제거 등)을 추가할 수 있습니다.
        val formattedPhone = phoneNumber.replace("-", "").trim()
        return repository.requestPhoneAuthCode(formattedPhone)
    }
}