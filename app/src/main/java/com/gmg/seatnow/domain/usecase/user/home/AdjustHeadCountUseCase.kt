package com.gmg.seatnow.domain.usecase.user.home

import javax.inject.Inject

class AdjustHeadCountUseCase @Inject constructor() {
    operator fun invoke(currentHeadCount: String, amount: Int): String {
        val current = currentHeadCount.toIntOrNull() ?: 1
        val next = (current + amount).coerceIn(1, 99)
        return next.toString()
    }
}
