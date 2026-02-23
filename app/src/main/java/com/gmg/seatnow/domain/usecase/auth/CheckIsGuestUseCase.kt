package com.gmg.seatnow.domain.usecase.auth

import com.gmg.seatnow.data.local.AuthManager
import javax.inject.Inject

class CheckIsGuestUseCase @Inject constructor(
    private val authManager: AuthManager
) {
    operator fun invoke(): Boolean {
        // 토큰이 없으면 게스트로 판단
        return !authManager.hasToken() 
    }
}