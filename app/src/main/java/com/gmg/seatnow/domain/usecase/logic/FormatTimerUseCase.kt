package com.gmg.seatnow.domain.usecase.logic

import javax.inject.Inject

class FormatTimerUseCase @Inject constructor() {
    operator fun invoke(secondsLeft: Int): String {
        val minutes = secondsLeft / 60
        val seconds = secondsLeft % 60
        return "%d:%02d".format(minutes, seconds)
    }
}