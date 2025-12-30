package com.gmg.seatnow.domain.usecase

import javax.inject.Inject

class ValidatePasswordUseCase @Inject constructor() {
    // [Move] ViewModel에 있던 정규식을 이곳으로 이동
    private val passwordRegex = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!@#\$%^&*()_+=-]).{8,20}\$".toRegex()

    operator fun invoke(password: String): Boolean {
        return password.isNotBlank() && password.matches(passwordRegex)
    }
}