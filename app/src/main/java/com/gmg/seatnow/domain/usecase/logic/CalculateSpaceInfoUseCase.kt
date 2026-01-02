package com.gmg.seatnow.domain.usecase.logic

import com.gmg.seatnow.presentation.owner.dataclass.SpaceItem
import com.gmg.seatnow.presentation.owner.dataclass.TableItem
import javax.inject.Inject

class CalculateSpaceInfoUseCase @Inject constructor() {

    // ViewModel의 saveSpaceItem 내부에 있던 복잡한 로직을 그대로 가져옴
    operator fun invoke(item: SpaceItem): SpaceItem {
        val inputName = item.editInput.trim()

        // 1. 이름 검사
        if (inputName.isBlank()) {
            return item.copy(inputError = "공간 이름을 입력해주세요.")
        }

        // 2. 유효한 테이블 필터링
        val validTables = item.tableList.filter {
            it.personCount.isNotBlank() && it.tableCount.isNotBlank()
        }

        // 3. 좌석 수 계산
        val totalSeats = validTables.sumOf {
            (it.personCount.toIntOrNull() ?: 0) * (it.tableCount.toIntOrNull() ?: 0)
        }

        // 4. 리스트 상태 결정 (비었으면 초기화, 아니면 저장)
        return if (validTables.isEmpty()) {
            item.copy(
                name = inputName,
                isEditing = false,
                inputError = null,
                seatCount = 0,
                tableList = listOf(TableItem(personCount = "", tableCount = ""))
            )
        } else {
            item.copy(
                name = inputName,
                isEditing = false,
                inputError = null,
                seatCount = totalSeats,
                tableList = validTables
            )
        }
    }
}