package com.gmg.seatnow.data.api

import com.gmg.seatnow.data.model.request.BusinessVerificationConfirmRequestDTO
import com.gmg.seatnow.data.model.request.EmailVerificationConfirmRequestDTO
import com.gmg.seatnow.data.model.request.EmailVerificationRequestDTO
import com.gmg.seatnow.data.model.request.OwnerLoginRequestDTO
import com.gmg.seatnow.data.model.request.OwnerWithdrawRequestDTO
import com.gmg.seatnow.data.model.request.SeatUpdateRequestDTO
import com.gmg.seatnow.data.model.request.SmsVerificationConfirmRequestDTO
import com.gmg.seatnow.data.model.request.SmsVerificationRequestDTO
import com.gmg.seatnow.data.model.response.BaseResponse
import com.gmg.seatnow.data.model.response.OwnerLoginResponseDTO
import com.gmg.seatnow.data.model.response.OwnerSignUpResponse
import com.gmg.seatnow.data.model.response.PlaceSearchResponseDTO
import com.gmg.seatnow.data.model.response.SeatStatusResponseDTO
import com.gmg.seatnow.data.model.response.KakaoLoginResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

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

    // 키워드 장소 검색 (상호명 검색)
    @GET("/api/v1/places/search")
    suspend fun searchPlaces(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 15
    ): Response<BaseResponse<List<PlaceSearchResponseDTO>>>

    // 좌표 기반 주변 대학 조회
    @GET("/api/v1/places/universities")
    suspend fun getNearbyUniversities(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): Response<BaseResponse<List<String>>>

    // 회원가입 완료
    @Multipart
    @POST("/api/v1/stores/owner/signup") // ★ 실제 엔드포인트 확인 필요
    suspend fun signUpOwner(
        @Part("signupData") request: RequestBody, // JSON 데이터
        @Part licenseImage: MultipartBody.Part?, // 사업자 등록증
        @Part storeImages: List<MultipartBody.Part> // 가게 사진들
    ): Response<BaseResponse<OwnerSignUpResponse>>// 성공 여부 반환

    // 사장님 로그인
    @POST("/auth/login/owner")
    suspend fun loginOwner(
        @Body request: OwnerLoginRequestDTO
    ): Response<BaseResponse<OwnerLoginResponseDTO>>

    // 토큰 재발급
    @POST("/api/v1/auth/reissue") // v1 경로 확인 필요 (스웨거에는 /auth/reissue로 되어있는데 보통 /api/v1 붙음)
    suspend fun reissueToken(
        @Header("RefreshToken") refreshToken: String
    ): Response<BaseResponse<OwnerLoginResponseDTO>>

    // 사장님 회원탈퇴
    @HTTP(method = "DELETE", path = "/api/v1/stores/owner", hasBody = true)
    suspend fun withdrawOwner(
        @Body request: OwnerWithdrawRequestDTO
    ): Response<BaseResponse<Unit>>

    // 사장님 로그아웃
    @POST("/auth/logout")
    suspend fun logout(): Response<BaseResponse<Unit>> // data가 {} 빈 객체이므로 Unit 또는 Any 처리

    // 사장님 좌석조회
    @GET("/api/v1/stores/{storeId}/seats")
    suspend fun getSeatStatus(
        @Path("storeId") storeId: Long
    ): Response<BaseResponse<SeatStatusResponseDTO>> // 방금 만든 DTO 사용

    // 사장님 좌석 실시간 업데이트
    @PATCH("/api/v1/stores/seats")
    suspend fun updateSeatStatus(
        @Body request: SeatUpdateRequestDTO
    ): Response<BaseResponse<SeatStatusResponseDTO>>

    // 카카오 로그인
    @GET("/auth/login/kakao")
    suspend fun loginWithKakao(
        @Query("kakaoAccessToken") kakaoAccessToken: String
    ): Response<BaseResponse<KakaoLoginResponse>>

    @DELETE("/api/v1/users")
    suspend fun withdrawUser(): Response<BaseResponse<Unit>>
}