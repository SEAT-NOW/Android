package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.model.SpaceItem
import com.gmg.seatnow.domain.repository.SeatRepository
import javax.inject.Inject

class UpdateStoreLayoutUseCase @Inject constructor(
    private val repository: SeatRepository
) {
    suspend operator fun invoke(spaceList: List<SpaceItem>) = repository.updateStoreLayout(spaceList)
}