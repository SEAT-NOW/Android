package com.gmg.seatnow.domain.usecase.user

import javax.inject.Inject

class ValidateHeadCountUseCase @Inject constructor() {
    operator fun invoke(count: String): String? {
        if (count.isEmpty()) return ""
        if (!count.all { it.isDigit() }) return null
        if (count.length > 2) return "99"
        val number = count.toIntOrNull()
        if (number == null || number == 0) return null
        return count
    }
}
