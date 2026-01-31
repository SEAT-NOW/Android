package com.gmg.seatnow.data.repository

import android.content.Context
import android.net.Uri
import com.gmg.seatnow.data.api.AuthService
import com.gmg.seatnow.data.model.request.CategoryUpdateDto
import com.gmg.seatnow.data.model.request.MenuDataRequest
import com.gmg.seatnow.data.model.request.StoreOperationRequest
import com.gmg.seatnow.data.model.request.UpdateMenuCategoriesRequest
import com.gmg.seatnow.data.model.request.RegularHolidayRequest
import com.gmg.seatnow.data.model.request.TemporaryHolidayRequest
import com.gmg.seatnow.data.model.request.HourRequest
import com.gmg.seatnow.domain.model.OpeningHour
import com.gmg.seatnow.domain.model.RegularHoliday
import com.gmg.seatnow.domain.model.StoreMenuCategory
import com.gmg.seatnow.domain.model.StoreMenuItemData
import com.gmg.seatnow.domain.model.StoreOperationInfo
import com.gmg.seatnow.domain.model.TemporaryHoliday
import com.gmg.seatnow.domain.repository.StoreRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import javax.inject.Inject

// ★ [핵심] 이 어노테이션이 있어야 "Error: moved to extension" 오류를 무시하고 빌드됩니다.
@Suppress("DEPRECATION", "DEPRECATION_ERROR")
class StoreRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    @ApplicationContext private val context: Context
) : StoreRepository {

    private var cachedMenus: List<StoreMenuCategory>? = null

    override suspend fun getStoreMenus(forceRefresh: Boolean): Result<List<StoreMenuCategory>> {
        if (!forceRefresh && cachedMenus != null) {
            return Result.success(cachedMenus!!)
        }

        return try {
            val response = authService.getStoreMenus()

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                val decimalFormat = DecimalFormat("#,###")

                val mappedData = data?.categories?.map { category ->
                    StoreMenuCategory(
                        id = category.id,
                        name = category.name,
                        items = category.menus?.map { menu ->
                            StoreMenuItemData(
                                id = menu.id,
                                name = menu.name,
                                price = decimalFormat.format(menu.price),
                                imageUrl = menu.imageUrl
                            )
                        } ?: emptyList()
                    )
                } ?: emptyList()

                cachedMenus = mappedData
                Result.success(mappedData)
            } else {
                Result.failure(Exception("메뉴 조회 실패"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getStoreOperations(): Result<StoreOperationInfo> {
        return try {
            val response = authService.getStoreOperations()
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    val info = StoreOperationInfo(
                        operationStatus = data.operationStatus ?: "CLOSED",
                        regularHolidays = data.regularHolidays.map {
                            RegularHoliday(it.dayOfWeek, it.weekInfo)
                        },
                        temporaryHolidays = data.temporaryHolidays.map {
                            TemporaryHoliday(it.startDate, it.endDate)
                        },
                        openingHours = data.openingHours.map {
                            OpeningHour(it.dayOfWeek, it.startTime, it.endTime)
                        }
                    )
                    Result.success(info)
                } else {
                    Result.failure(Exception("데이터가 없습니다."))
                }
            } else {
                Result.failure(Exception("운영 정보 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStoreImages(): Result<List<String>> {
        return try {
            val response = authService.getStoreImages()
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                if (data != null) {
                    val sortedImages = data.storeImages
                        .sortedByDescending { it.main }
                        .map { it.imageUrl }

                    Result.success(sortedImages)
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(Exception("사진 조회 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStoreOperation(
        regularHolidays: List<RegularHoliday>,
        temporaryHolidays: List<TemporaryHoliday>,
        openingHours: List<OpeningHour>
    ): Result<Unit> {
        return try {
            val request = StoreOperationRequest(
                regularHolidays = regularHolidays.map {
                    RegularHolidayRequest(dayOfWeek = it.dayOfWeek, weekInfo = it.weekInfo)
                },
                temporaryHolidays = temporaryHolidays.map {
                    TemporaryHolidayRequest(startDate = it.startDate, endDate = it.endDate)
                },
                hours = openingHours.map {
                    HourRequest(dayOfWeek = it.dayOfWeek, startTime = it.startTime, endTime = it.endTime)
                }
            )

            val response = authService.updateStoreOperation(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "수정 실패"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun updateMenuCategories(categories: List<StoreMenuCategory>): Result<Boolean> {
        return try {
            val categoryDtos = categories.map { category ->
                CategoryUpdateDto(
                    id = if (category.id > 0) category.id else null,
                    name = category.name
                )
            }

            val request = UpdateMenuCategoriesRequest(categoryDtos)
            val response = authService.updateMenuCategories(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                val errorMsg = response.body()?.message ?: "카테고리 수정 실패"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveMenu(
        menuId: Long?,
        categoryId: Long,
        name: String,
        price: Int,
        imageUri: String?,
        isImageChanged: Boolean
    ): Result<Boolean> {
        return try {
            val keepImage = !isImageChanged && !imageUri.isNullOrEmpty()

            val requestDto = MenuDataRequest(
                id = if (menuId != null && menuId > 0) menuId else null,
                name = name,
                price = price,
                categoryId = categoryId,
                keepImage = keepImage
            )

            val jsonString = Gson().toJson(requestDto)

            // ★ [수정] 확장 함수(toMediaTypeOrNull) 대신 Static Method(parse) 강제 사용
            // @Suppress("DEPRECATION_ERROR") 덕분에 에러가 나지 않습니다.
            val mediaType = MediaType.parse("application/json")

            // ★ [수정] 확장 함수(toRequestBody) 대신 Static Method(create) 강제 사용
            // Ambiguity 오류를 피하는 가장 확실한 방법입니다.
            val jsonRequestBody = RequestBody.create(mediaType, jsonString)

            var imagePart: MultipartBody.Part? = null
            if (isImageChanged && !imageUri.isNullOrEmpty()) {
                val file = uriToFile(Uri.parse(imageUri))
                if (file != null) {
                    // 여기도 동일하게 수정
                    val imageMediaType = MediaType.parse("image/*")
                    val requestFile = RequestBody.create(imageMediaType, file)
                    imagePart = MultipartBody.Part.createFormData("menuImage", file.name, requestFile)
                }
            }

            val response = authService.saveMenu(jsonRequestBody, imagePart)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                val errorMsg = response.body()?.message ?: "메뉴 저장 실패"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            tempFile
        } catch (e: Exception) {
            null
        }
    }
}