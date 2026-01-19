package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.repository.SeatRepository
import com.gmg.seatnow.domain.repository.SeatStatusData
import javax.inject.Inject

class GetSeatStatusUseCase @Inject constructor(
    private val repository: SeatRepository
) {
    suspend operator fun invoke(): Result<SeatStatusData> {
        return repository.getSeatStatus()
    }
}