package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.model.OpeningHour
import com.gmg.seatnow.domain.model.RegularHoliday
import com.gmg.seatnow.domain.model.TemporaryHoliday
import com.gmg.seatnow.domain.repository.StoreRepository
import javax.inject.Inject

class UpdateStoreOperationInfoUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(
        regularHolidays: List<RegularHoliday>,
        temporaryHolidays: List<TemporaryHoliday>,
        openingHours: List<OpeningHour>
    ): Result<Unit> {
        return repository.updateStoreOperation(regularHolidays, temporaryHolidays, openingHours)
    }
}