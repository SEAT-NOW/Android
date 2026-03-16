package com.gmg.seatnow.domain.usecase.user.auth

import com.gmg.seatnow.data.local.UserManager
import javax.inject.Inject

class CheckKakaoTermsUseCase @Inject constructor(
    private val userManager: UserManager
) {
    operator fun invoke(): Boolean {
        return userManager.isKakaoTermsAgreed()
    }
}