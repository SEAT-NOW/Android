package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.repository.MapRepository
import javax.inject.Inject

class ToggleMenuLikeUseCase @Inject constructor(
    private val repository: MapRepository
) {
    suspend operator fun invoke(menuId: Long): Result<Boolean> {
        return repository.toggleMenuLike(menuId)
    }
}