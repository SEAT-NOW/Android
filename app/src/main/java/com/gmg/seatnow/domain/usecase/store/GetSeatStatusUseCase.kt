package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.repository.SeatRepository
import com.gmg.seatnow.domain.repository.SeatStatusData
import javax.inject.Inject

class GetSeatStatusUseCase @Inject constructor(
    private val repository: SeatRepository
) {
    // 수정: forceRefresh 파라미터 추가 (기본값 false 설정 추천)
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<SeatStatusData> {
        return repository.getSeatStatus(forceRefresh)
    }
}