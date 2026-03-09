package com.gmg.seatnow.domain.usecase.common.validation

import javax.inject.Inject

class ValidatePasswordCheckUseCase @Inject constructor() {
    operator fun invoke(password: String, check: String): String? {
        if (check.isBlank()) return null
        return if (check != password) "비밀번호가 일치하지 않습니다." else null
    }
}
