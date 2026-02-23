package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.domain.repository.MapRepository
import javax.inject.Inject

class ToggleStoreKeepUseCase @Inject constructor(
    private val repository: MapRepository,
    private val authManager: AuthManager
) {
    suspend operator fun invoke(storeId: Long, isKept: Boolean): Result<Unit> {
        if (!authManager.hasToken()) {
            return Result.failure(Exception("LOGIN_REQUIRED")) // 약속된 에러 메시지
        }
        return repository.toggleStoreKeep(storeId, isKept)
    }
}