package com.gmg.seatnow.domain.usecase.user.auth

import com.gmg.seatnow.data.local.AppConfigManager
import com.gmg.seatnow.data.local.UserManager
import javax.inject.Inject

class SetDeveloperModeUseCase @Inject constructor(
    private val appConfigManager: AppConfigManager,
    private val userManager: UserManager
) {
    operator fun invoke() {
        appConfigManager.setTesterMode(true)
        userManager.saveUserInfo("개발자(Tester)")
    }
}