package com.gmg.seatnow.domain.usecase.common.validation

import javax.inject.Inject

class CheckTestAccountUseCase @Inject constructor() {
    fun isTestEmail(email: String): Boolean {
        return email == "reviewer@seatnow.com" || email.startsWith("test")
    }

    fun isTestPhone(phone: String): Boolean {
        return phone == "01000000000"
    }

    fun isTestBusinessNum(num: String): Boolean {
        return num == "0000000000"
    }
}