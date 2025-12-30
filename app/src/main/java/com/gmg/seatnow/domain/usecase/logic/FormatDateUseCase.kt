package com.gmg.seatnow.domain.usecase.logic

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class FormatDateUseCase @Inject constructor() {
    operator fun invoke(millis: Long?): String {
        if (millis == null) return ""
        return SimpleDateFormat("yyyy/MM/dd", Locale.KOREA).format(Date(millis))
    }
}