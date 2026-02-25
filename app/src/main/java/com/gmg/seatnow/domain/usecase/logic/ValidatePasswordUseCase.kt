package com.gmg.seatnow.domain.usecase.logic

import javax.inject.Inject

class ValidatePasswordUseCase @Inject constructor() {
    private val passwordRegex = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!@#\$%^&*()_+=-]).{8,20}\$".toRegex()

    operator fun invoke(password: String): String? {
        if (password.isBlank()) return null
        if (!password.matches(passwordRegex)) return "영문, 숫자, 특수문자 포함 8~20자리여야 합니다."
        return null
    }
}