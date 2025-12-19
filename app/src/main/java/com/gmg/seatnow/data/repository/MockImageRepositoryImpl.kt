package com.gmg.seatnow.data.repository

import android.net.Uri
import kotlinx.coroutines.delay
import javax.inject.Inject

class MockImageRepositoryImpl @Inject constructor() : ImageRepository {

    override suspend fun uploadImage(uri: Uri): Result<String> {
        // 1. 네트워크 통신 흉내 (1.5초 딜레이)
        delay(1500)

        // 2. 무조건 성공했다고 가정하고 가짜 URL 리턴
        // (실제 앱에서는 이 URL을 Coil이 로드하려고 시도할 것입니다.)
        val mockSuccessUrl = "https://via.placeholder.com/150/0000FF/808080?text=Upload+Success"
        
        // 만약 실패 케이스를 테스트하고 싶다면 아래 주석을 풀고 위를 주석처리 하세요.
        // return Result.failure(Exception("네트워크 연결 실패 (테스트)"))

        return Result.success(mockSuccessUrl)
    }
}