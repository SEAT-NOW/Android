package com.gmg.seatnow.domain.usecase.auth

import com.gmg.seatnow.data.local.UserManager
import javax.inject.Inject

class CheckGuestTermsUseCase @Inject constructor(
    private val userManager: UserManager
) {
    operator fun invoke(): Boolean {
        return userManager.isGuestTermsAgreed()
    }
}