// com.gmg.seatnow.domain.usecase.user.auth.GetSplashDestinationUseCase.kt
package com.gmg.seatnow.domain.usecase.user.auth

import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.data.local.UserManager
import com.gmg.seatnow.domain.model.SplashDestination
import javax.inject.Inject

class GetSplashDestinationUseCase @Inject constructor(
    private val authManager: AuthManager,
    private val userManager: UserManager
) {
    operator fun invoke(): SplashDestination {
        val hasToken = authManager.hasToken()
        val storeId = authManager.getStoreId()
        val isGuestAgreed = userManager.isGuestTermsAgreed()
        val isKakaoAgreed = userManager.isKakaoTermsAgreed()

        return if (hasToken) {
            if (storeId != -1L) SplashDestination.OWNER_MAIN
            else if (isKakaoAgreed) SplashDestination.USER_MAIN
            else SplashDestination.TERMS_KAKAO
        } else if (isGuestAgreed) {
            SplashDestination.USER_MAIN
        } else {
            SplashDestination.LOGIN
        }
    }
}