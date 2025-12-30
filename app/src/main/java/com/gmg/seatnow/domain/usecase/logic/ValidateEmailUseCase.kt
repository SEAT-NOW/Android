package com.gmg.seatnow.domain.usecase.logic

import javax.inject.Inject

class ValidateEmailUseCase @Inject constructor() {
    // [Move] ViewModel에 있던 정규식을 이곳으로 이동
    private val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$".toRegex()

    operator fun invoke(email: String): Boolean {
        return email.isNotBlank() && email.matches(emailRegex)
    }
}