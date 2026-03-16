package com.gmg.seatnow.domain.usecase.owner.seat

import com.gmg.seatnow.domain.model.TableItem
import javax.inject.Inject

class UpdateTableCountUseCase @Inject constructor() {
    operator fun invoke(allTables: List<TableItem>, itemId: String, delta: Int): List<TableItem> {
        val newRawList = allTables.toMutableList()
        val index = newRawList.indexOfFirst { it.id == itemId }

        if (index != -1) {
            val item = newRawList[index]
            val newCount = (item.currentCount + delta).coerceIn(0, item.maxTableCount)
            newRawList[index] = item.copy(currentCount = newCount)
        }

        return newRawList
    }
}
