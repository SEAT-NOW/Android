package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.domain.repository.MapRepository
import javax.inject.Inject

class ToggleMenuLikeUseCase @Inject constructor(
    private val repository: MapRepository,
    private val authManager: AuthManager
) {
    suspend operator fun invoke(menuId: Long): Result<Boolean> {
        if (!authManager.hasToken()) {
            return Result.failure(Exception("LOGIN_REQUIRED")) // 약속된 에러 메시지
        }

        return repository.toggleMenuLike(menuId)
    }
}