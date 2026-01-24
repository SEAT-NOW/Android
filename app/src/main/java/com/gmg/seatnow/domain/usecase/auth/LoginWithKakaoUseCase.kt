package com.gmg.seatnow.domain.usecase.auth

import com.gmg.seatnow.domain.model.KakaoLoginResult
import com.gmg.seatnow.domain.repository.AuthRepository
import javax.inject.Inject

class LoginWithKakaoUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<KakaoLoginResult> {
        return authRepository.loginKakao()
    }
}