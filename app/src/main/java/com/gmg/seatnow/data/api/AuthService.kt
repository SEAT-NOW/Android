package com.gmg.seatnow.data.api

import com.gmg.seatnow.data.model.request.SmsVerificationConfirmRequest
import com.gmg.seatnow.data.model.request.SmsVerificationRequest
import com.gmg.seatnow.data.model.response.BaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    // SMS 인증 코드 발송 API
    @POST("/api/v1/auth/verifications/sms")
    suspend fun sendSmsVerification(
        @Body request: SmsVerificationRequest
    ): Response<BaseResponse<Boolean>> // data가 boolean인 것으로 보임

    // 2. SMS 인증번호 검증
    @POST("/api/v1/auth/verifications/sms/confirm")
    suspend fun verifySmsCode(
        @Body request: SmsVerificationConfirmRequest
    ): Response<BaseResponse<Boolean>>

}