package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.model.TableItem
import com.gmg.seatnow.domain.repository.SeatRepository
import javax.inject.Inject

// 뷰모델에서 호출할 유스케이스 (저장 로직 캡슐화)
class UpdateSeatUsageUseCase @Inject constructor(
    private val repository: SeatRepository
) {
    suspend operator fun invoke(items: List<TableItem>): Result<Unit> {
        // 필요하다면 여기서 데이터 유효성 검사 등의 비즈니스 로직을 추가할 수 있습니다.
        return repository.updateSeatUsage(items)
    }
}