package com.gmg.seatnow.domain.usecase.common.validation

import javax.inject.Inject

class ValidateEmailUseCase @Inject constructor() {
    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$".toRegex()

    operator fun invoke(email: String): String? {
        if (email.isBlank()) return null
        if (!email.matches(emailRegex)) return "올바른 이메일 형식이 아닙니다."
        return null
    }
}