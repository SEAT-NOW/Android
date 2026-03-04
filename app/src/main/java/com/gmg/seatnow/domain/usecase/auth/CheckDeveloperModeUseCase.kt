package com.gmg.seatnow.domain.usecase.auth

import com.gmg.seatnow.data.local.AppConfigManager
import javax.inject.Inject

class CheckDeveloperModeUseCase @Inject constructor(
    private val appConfigManager: AppConfigManager
) {
    operator fun invoke(): Boolean {
        return appConfigManager.isTester()
    }
}
