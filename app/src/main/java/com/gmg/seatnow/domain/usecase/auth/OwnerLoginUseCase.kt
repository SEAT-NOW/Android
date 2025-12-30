package com.gmg.seatnow.domain.usecase.auth

import com.gmg.seatnow.domain.repository.AuthRepository
import javax.inject.Inject

// 사장님 로그인 "행위"를 담당하는 UseCase
class OwnerLoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    // operator fun invoke를 쓰면 useCase() 처럼 함수처럼 호출 가능
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        // 나중에 여기에 "로그인 시도 횟수 제한" 같은 비즈니스 로직이 추가될 수 있음
        return repository.loginOwner(email, password)
    }
}