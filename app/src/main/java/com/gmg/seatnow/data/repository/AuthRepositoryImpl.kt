package com.gmg.seatnow.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.gmg.seatnow.data.api.AuthService
import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.data.model.request.BusinessVerificationConfirmRequestDTO
import com.gmg.seatnow.data.model.request.EmailVerificationConfirmRequestDTO
import com.gmg.seatnow.data.model.request.EmailVerificationRequestDTO
import com.gmg.seatnow.data.model.request.OwnerLoginRequestDTO
import com.gmg.seatnow.data.model.request.OwnerSignUpRequestDTO
import com.gmg.seatnow.data.model.request.OwnerWithdrawRequestDTO
import com.gmg.seatnow.data.model.request.SmsVerificationConfirmRequestDTO
import com.gmg.seatnow.data.model.request.SmsVerificationRequestDTO
import com.gmg.seatnow.data.model.request.StorePhoneUpdateRequestDTO
import com.gmg.seatnow.data.model.request.VerifyPasswordRequestDTO
import com.gmg.seatnow.data.model.response.ChangePasswordRequestDTO
import com.gmg.seatnow.data.model.response.ErrorResponse
import com.gmg.seatnow.data.model.response.OwnerAccountResponseDTO
import com.gmg.seatnow.data.model.response.StoreProfileResponseDTO
import com.gmg.seatnow.domain.model.KakaoLoginResult
import com.gmg.seatnow.domain.model.StoreSearchResult
import com.gmg.seatnow.domain.repository.AuthRepository
import com.google.gson.Gson
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authManager: AuthManager,
    private val authService: AuthService
) : AuthRepository {

    private var cachedOwnerAccount: OwnerAccountResponseDTO? = null

    override suspend fun loginKakao(): Result<KakaoLoginResult> {
        return try {
            // 1. 카카오 SDK로 토큰 발급
            val kakaoToken = getKakaoAccessToken()

            // 2. 백엔드 API 호출 (Query Parameter 전송)
            val response = authService.loginWithKakao(kakaoToken)

            if (response.isSuccessful && response.body()?.success == true) {
                // 3. 응답 데이터 추출 (KakaoLoginResponse)
                val responseData = response.body()?.data ?: throw Exception("응답 데이터가 없습니다.")

                // 4. "Bearer " 접두사 처리
                val appAccessToken = if (responseData.accessToken.startsWith("Bearer")) {
                    responseData.accessToken
                } else {
                    "Bearer ${responseData.accessToken}"
                }

                // 5. AuthManager에 로컬 저장 (storeId가 null이면 -1L로 저장)
                authManager.saveLoginData(
                    accessToken = appAccessToken,
                    refreshToken = responseData.refreshToken,
                    storeId = responseData.storeId ?: -1L
                )

                // 6. Domain 모델로 매핑하여 반환 (toDomain 함수 사용)
                Result.success(responseData.toDomain(appAccessToken))

            } else {
                // ... 기존 에러 파싱 로직 (parseErrorMessage 등)
                Result.failure(Exception("서버 로그인 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 카카오 SDK 로그인을 코루틴으로 감싼 내부 함수
    private suspend fun getKakaoAccessToken(): String = suspendCoroutine { continuation ->
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    UserApiClient.instance.loginWithKakaoAccount(context) { tokenWeb, errorWeb ->
                        if (errorWeb != null) continuation.resumeWithException(errorWeb)
                        else if (tokenWeb != null) continuation.resume(tokenWeb.accessToken)
                    }
                } else if (token != null) continuation.resume(token.accessToken)
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
                if (error != null) continuation.resumeWithException(error)
                else if (token != null) continuation.resume(token.accessToken)
            }
        }
    }

    override suspend fun loginOwner(email: String, password: String): Result<Unit> {
        return try {
            val request = OwnerLoginRequestDTO(email, password)
            val response = authService.loginOwner(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                data?.let {
                    // Bearer 접두사 처리
                    val accessToken = if (it.accessToken.startsWith("Bearer")) it.accessToken else "Bearer ${it.accessToken}"
                    // Refresh Token은 보통 Bearer 안 붙지만, 서버 스펙에 따라 다름. 일단 그대로 저장.
                    val refreshToken = it.refreshToken
                    val storeId = it.storeId

                    authManager.saveLoginData(accessToken, refreshToken, storeId) // ★ 둘 다 저장
                }
                Result.success(Unit)
            } else {
                // 4. 실패 처리
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Log.e("AuthRepo", "로그인 실패: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun reissueToken(): Result<Unit> {
        val refreshToken = authManager.getRefreshToken() ?: return Result.failure(Exception("Refresh Token 없음"))

        return try {
            // 헤더에 RefreshToken 실어서 요청
            val response = authService.reissueToken(refreshToken)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                data?.let {
                    // 새 토큰들 저장
                    val newAccessToken = if (it.accessToken.startsWith("Bearer")) it.accessToken else "Bearer ${it.accessToken}"
                    val newRefreshToken = it.refreshToken

                    authManager.saveTokens(newAccessToken, newRefreshToken)
                }
                Log.d("AuthRepo", "토큰 재발급 성공")
                Result.success(Unit)
            } else {
                // 재발급 실패 (Refresh Token 만료 등) -> 로그아웃 처리
                authManager.clearTokens()
                Log.e("AuthRepo", "토큰 재발급 실패: ${response.code()}")
                Result.failure(Exception("토큰 만료"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ★ [추가] 인증번호 전송 구현 (Mock)
    override suspend fun requestEmailAuthCode(email: String): Result<Unit> {
        return try {
            val request = EmailVerificationRequestDTO(email = email)
            val response = authService.sendEmailVerification(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMessage = response.body()?.message
                    ?: response.errorBody()?.string()
                    ?: "인증번호 발송에 실패했습니다."
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun requestPhoneAuthCode(phoneNumber: String): Result<Unit> {
        return try {
            val request = SmsVerificationRequestDTO(phoneNumber = phoneNumber)
            val response = authService.sendSmsVerification(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                // 실패 시 에러 메시지 파싱
                val errorMessage = response.body()?.message
                    ?: response.errorBody()?.string()
                    ?: "인증번호 발송에 실패했습니다."
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ★ [추가] 인증번호 검증 구현 (Mock)
    override suspend fun verifyEmailAuthCode(email: String, code: String): Result<Unit> {
        return try {
            val request = EmailVerificationConfirmRequestDTO(email, code)
            val response = authService.verifyEmailCode(request)

            if (response.isSuccessful && response.body()?.success == true) {
                // 200 OK
                Result.success(Unit)
            } else {
                // 400 Bad Request 등 에러 처리
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun verifyPhoneAuthCode(phoneNumber: String, code: String): Result<Unit> {
        return try {
            val request = SmsVerificationConfirmRequestDTO(phoneNumber, code)
            val response = authService.verifySmsCode(request)

            if (response.isSuccessful && response.body()?.success == true) {
                // 200 OK
                Result.success(Unit)
            } else {
                // 400 Bad Request 등 에러 처리
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // 사업자 번호 검증 Mock
    override suspend fun verifyBusinessNumber(businessNumber: String): Result<Unit> {
        return try {
            val request = BusinessVerificationConfirmRequestDTO(businessNumber)
            val response = authService.verifyBusinessNumber(request)

            if (response.isSuccessful && response.body()?.success == true) {
                // 200 OK
                Result.success(Unit)
            } else {
                // 400 Bad Request 등 에러 처리
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // 상호명 검색 Mock
    override suspend fun searchStore(query: String): Result<List<StoreSearchResult>> {
        if (query.isBlank()) return Result.success(emptyList())

        return try {
            // API 호출 (기본 page=1, size=15 사용)
            val response = authService.searchPlaces(query = query)

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data ?: emptyList()

                // DTO -> Domain Model 변환
                val mappedList = data.map { dto ->
                    StoreSearchResult(
                        placeName = dto.name,
                        addressName = dto.roadAddress,
                        latitude = dto.lat,
                        longitude = dto.lng
                    )
                }
                Result.success(mappedList)
            } else {
                // 에러 파싱
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // 주변 대학 찾기 Mock
    override suspend fun getNearbyUniversity(lat: Double, lng: Double): Result<List<String>> {
        return try {
            // API 호출
            val response = authService.getNearbyUniversities(lat = lat, lng = lng)

            if (response.isSuccessful && response.body()?.success == true) {
                // data가 null이면 빈 리스트 반환
                val univList = response.body()?.data ?: emptyList()
                Result.success(univList)
            } else {
                // 에러 파싱
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun signUpOwner(
        requestDto: OwnerSignUpRequestDTO,
        licenseUri: Uri?,
        storeImageUris: List<Uri>
    ): Result<Unit> {
        return try {
            val jsonString = Gson().toJson(requestDto)
            val requestBody = jsonString.toRequestBody("application/json".toMediaTypeOrNull())

            val licensePart = licenseUri?.let { uri ->
                if (uri.toString().startsWith("http")) null
                else prepareFilePart("licenseImage", uri)
            }
            val imageParts = storeImageUris.mapNotNull { uri ->
                if (uri.toString().startsWith("http")) null
                else prepareFilePart("storeImages", uri)
            }

            val response = authService.signUpOwner(requestBody, licensePart, imageParts)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // 리턴 타입을 MultipartBody.Part? (Nullable)로 변경
    private fun prepareFilePart(partName: String, fileUri: Uri): MultipartBody.Part? {
        return try {
            val file = File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.jpg")

            val inputStream = context.contentResolver.openInputStream(fileUri)
                ?: return null // 스트림을 못 열면 null 반환

            inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, file.name, requestFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null // 에러 발생 시 null 반환 (앱 크래시 방지)
        }
    }

    override suspend fun ownerLogout(): Result<Unit> {
        return try {
            // 1. 서버에 로그아웃 요청 (Refresh Token 삭제 요청)
            val response = authService.logout()

            if (response.isSuccessful && response.body()?.success == true) {
                Log.d("AuthRepo", "서버 로그아웃 성공")
                Result.success(Unit)
            } else {
                // 실패했더라도 로컬 로그아웃은 진행해야 함
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Log.e("AuthRepo", "서버 로그아웃 실패 (하지만 로컬 데이터는 삭제함): $errorMsg")
                Result.success(Unit) // 로컬 처리는 되었으므로 Success 처리 (선택 사항)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 네트워크 에러가 나도 앱 내에서는 로그아웃 처리
            Result.success(Unit)
        } finally {
            // ★ [핵심] 성공이든 실패든 앱 내부 저장소의 토큰은 무조건 삭제
            authManager.clearTokens()
        }
    }

    override suspend fun ownerWithdraw(businessNumber: String, password: String): Result<Unit> {
        return try {
            // 1. API 요청 DTO 생성
            // API 명세서 예시에 하이픈(-)이 포함되어 있으므로 포맷팅 (선택사항, 백엔드 로직에 따라 다름)
            // 여기서는 UI에서 넘어온 값(숫자만 있는 값이라 가정)에 하이픈을 넣어줍니다.
            val formattedBusinessNum = if (businessNumber.length == 10 && !businessNumber.contains("-")) {
                "${businessNumber.substring(0, 3)}-${businessNumber.substring(3, 5)}-${businessNumber.substring(5)}"
            } else {
                businessNumber
            }

            val request = OwnerWithdrawRequestDTO(
                businessNumber = formattedBusinessNum,
                password = password
            )

            // 2. API 호출
            val response = authService.withdrawOwner(request)

            if (response.isSuccessful && response.body()?.success == true) {
                // 3. 성공 시 내부 토큰 삭제
                authManager.clearTokens()
                Log.d("AuthRepo", "회원탈퇴 성공: 토큰 삭제 완료")
                Result.success(Unit)
            } else {
                // 4. 실패 시 에러 파싱 (4004 비밀번호 불일치 등)
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun withdrawUser(): Result<Unit> {
        return try {
            val response = authService.withdrawUser()
            val body = response.body()

            // 통신 성공 & BaseResponse의 success가 true일 때
            if (response.isSuccessful && body?.success == true) {
                Result.success(Unit)
            } else {
                // 서버에서 내려준 message를 에러 메시지로 사용
                Result.failure(Exception(body?.message ?: "회원탈퇴에 실패했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyOwnerPassword(password: String): Result<Unit> {
        return try {
            val response = authService.verifyOwnerPassword(VerifyPasswordRequestDTO(password))

            if (response.isSuccessful && response.body()?.success == true) {
                // 200 OK: 확인 성공
                Result.success(Unit)
            } else {
                // 400(비번 불일치), 404(유저 없음) 등 에러 처리
                // parseErrorMessage는 기존 AuthRepositoryImpl에 있는 함수 재사용
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun changeOwnerPassword(password: String): Result<Unit> {
        return try {
            val response = authService.changeOwnerPassword(ChangePasswordRequestDTO(password))

            if (response.isSuccessful && response.body()?.success == true) {
                // 200 OK
                Result.success(Unit)
            } else {
                // 에러 파싱
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateStorePhone(phone: String): Result<Unit> {
        return try {
            val response = authService.updateStorePhone(StorePhoneUpdateRequestDTO(phone))

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getStoreProfile(): Result<StoreProfileResponseDTO> {
        return try {
            val response = authService.getStoreProfile()

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("데이터가 비어있습니다."))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getOwnerAccount(): Result<OwnerAccountResponseDTO> {
        // 1. 캐시된 데이터가 있으면 바로 반환 (API 호출 X)
        cachedOwnerAccount?.let {
            return Result.success(it)
        }

        // 2. 캐시가 없으면 API 호출
        return try {
            val response = authService.getOwnerAccount()

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    // ★ 3. 성공 시 캐시에 저장
                    cachedOwnerAccount = data
                    Result.success(data)
                } else {
                    Result.failure(Exception("데이터가 비어있습니다."))
                }
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(errorBody: String?): String {
        return try {
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            errorResponse.message ?: errorResponse.detail ?: "알 수 없는 오류가 발생했습니다."
        } catch (e: Exception) {
            "서버 통신 오류가 발생했습니다."
        }
    }
}