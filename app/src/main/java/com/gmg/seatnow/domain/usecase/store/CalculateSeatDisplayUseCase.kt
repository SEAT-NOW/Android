package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.model.FloorCategory
import com.gmg.seatnow.domain.model.TableItem
import javax.inject.Inject

class CalculateSeatDisplayUseCase @Inject constructor() {
    data class Params(
        val allTables: List<TableItem>,
        val categories: List<FloorCategory>,
        val selectedCategoryId: String,
        val isEditMode: Boolean
    )

    data class Result(
        val groupedDisplayItems: Map<String, List<TableItem>>,
        val totalCapacity: Int,
        val usedSeats: Int
    )

    operator fun invoke(params: Params): Result {
        val resultMap = mutableMapOf<String, List<TableItem>>()

        if (params.selectedCategoryId == "ALL") {
            // [ALL 탭 로직]
            // 1. 전체 합계 섹션 생성 (수정 모드가 아닐 때만 보여줌!)
            if (!params.isEditMode) {
                val mergedList = params.allTables
                    .groupBy { it.label }
                    .map { (label, items) ->
                        TableItem(
                            id = "MERGED_$label",
                            floorId = "ALL",
                            label = label,
                            capacityPerTable = items.first().capacityPerTable,
                            maxTableCount = items.sumOf { it.maxTableCount },
                            currentCount = items.sumOf { it.currentCount }
                        )
                    }
                    .sortedBy { it.capacityPerTable }

                if (mergedList.isNotEmpty()) {
                    resultMap["전체"] = mergedList
                }
            }

            // 2. 층별 섹션 생성
            val floorCategories = params.categories.filter { it.id != "ALL" }

            floorCategories.forEach { category ->
                val floorItems = params.allTables.filter { it.floorId == category.id }
                if (floorItems.isNotEmpty()) {
                    resultMap[category.name] = floorItems
                }
            }

        } else {
            // [개별 층 탭 로직]
            val categoryName = params.categories.find { it.id == params.selectedCategoryId }?.name ?: ""
            val floorItems = params.allTables.filter { it.floorId == params.selectedCategoryId }
            resultMap[categoryName] = floorItems
        }

        // 전체 통계
        val totalCapacity = params.allTables.sumOf { it.capacityPerTable * it.maxTableCount }
        val usedSeats = params.allTables.sumOf { it.capacityPerTable * it.currentCount }

        return Result(resultMap, totalCapacity, usedSeats)
    }
}
