package com.gmg.seatnow.domain.usecase.owner.seat

import com.gmg.seatnow.domain.repository.SeatRepository
import javax.inject.Inject

class GetSeatConfigurationUseCase @Inject constructor(
    private val repository: SeatRepository
) {
    suspend operator fun invoke() = repository.getSeatStatus()
}