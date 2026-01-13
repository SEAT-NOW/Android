package com.gmg.seatnow.data.api

import com.gmg.seatnow.data.model.request.BusinessVerificationConfirmRequestDTO
import com.gmg.seatnow.data.model.request.EmailVerificationConfirmRequestDTO
import com.gmg.seatnow.data.model.request.EmailVerificationRequestDTO
import com.gmg.seatnow.data.model.request.SmsVerificationConfirmRequestDTO
import com.gmg.seatnow.data.model.request.SmsVerificationRequestDTO
import com.gmg.seatnow.data.model.response.BaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    // SMS 인증 코드 발송 API
    @POST("/api/v1/auth/verifications/sms")
    suspend fun sendSmsVerification(
        @Body request: SmsVerificationRequestDTO
    ): Response<BaseResponse<Boolean>> // data가 boolean인 것으로 보임

    // SMS 인증번호 검증
    @POST("/api/v1/auth/verifications/sms/confirm")
    suspend fun verifySmsCode(
        @Body request: SmsVerificationConfirmRequestDTO
    ): Response<BaseResponse<Boolean>>

    // Email 인증 코드 발송
    @POST("/api/v1/auth/verifications/email")
    suspend fun sendEmailVerification(
        @Body request: EmailVerificationRequestDTO
    ): Response<BaseResponse<Boolean>>

    // Email 인증번호 검증
    @POST("/api/v1/auth/verifications/email/confirm")
    suspend fun verifyEmailCode(
        @Body request: EmailVerificationConfirmRequestDTO
    ): Response<BaseResponse<Boolean>>

    // Business Number 등록번호 유효성 인증
    @POST("/api/v1/auth/verifications/business-number")
    suspend fun verifyBusinessNumber(
        @Body request: BusinessVerificationConfirmRequestDTO
    ): Response<BaseResponse<Boolean>>
}