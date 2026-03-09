package com.gmg.seatnow.domain.usecase.user.auth

import com.gmg.seatnow.data.local.UserManager
import javax.inject.Inject

class GetUserNicknameUseCase @Inject constructor(
    private val userManager: UserManager
) {
    operator fun invoke(): String? {
        // 닉네임은 이제 UserManager가 관리합니다.
        return userManager.getUserNickname()
    }
}