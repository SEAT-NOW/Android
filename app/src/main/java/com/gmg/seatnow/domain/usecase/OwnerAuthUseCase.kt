package com.gmg.seatnow.domain.usecase

import com.gmg.seatnow.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class OwnerAuthUseCase @Inject constructor(
    private val repository: AuthRepository // ★ Repository 주입
) {

    // 인증번호 전송 요청
    suspend fun requestAuthCode(target: String): Result<Unit> {
        // 필요하다면 여기서 비즈니스 로직(예: 이메일 형식 2차 검증 등)을 추가할 수 있습니다.
        return repository.requestAuthCode(target)
    }

    // 인증번호 검증 요청
    suspend fun verifyAuthCode(target: String, code: String): Result<Unit> {
        return repository.verifyAuthCode(target, code)
    }

    // 사업자 번호 검증 요청
    suspend fun verifyBusinessNumber(number: String) = repository.verifyBusinessNumber(number)

    //검색 기능 api 요청
    suspend fun searchStore(query: String) = repository.searchStore(query)

    // 근처 대학명 찾기 요청
    suspend fun getNearbyUniversity(lat: Double, lng: Double): Result<List<String>> {
        return repository.getNearbyUniversity(lat, lng)
    }
}