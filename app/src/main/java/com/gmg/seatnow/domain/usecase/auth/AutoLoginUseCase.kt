package com.gmg.seatnow.domain.usecase.auth

import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.domain.repository.AuthRepository
import javax.inject.Inject

class AutoLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val authManager: AuthManager
) {
    /**
     * @return true: 자동 로그인 성공 (메인으로 이동), false: 실패 (로그인 화면으로 이동)
     */
    suspend operator fun invoke(): Boolean {
        // 1. 로컬에 토큰이 있는지 확인
        if (!authManager.hasToken()) {
            return false // 토큰 없으면 로그인 필요
        }

        // 2. 토큰이 있다면, 유효한지 확인하기 위해 재발급 시도 (혹은 별도 검증 API)
        // (단순히 hasToken()만 믿으면 만료된 토큰일 수도 있으므로, 서버에 찔러보는 것이 안전함)
        val result = authRepository.reissueToken()
        
        return result.isSuccess
    }
}