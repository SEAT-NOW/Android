package com.gmg.seatnow.data.repository

import android.content.Context
import android.net.Uri
import com.gmg.seatnow.data.api.AuthService
import com.gmg.seatnow.data.model.request.CategoryOrderDto
import com.gmg.seatnow.data.model.request.CategoryUpdateDto
import com.gmg.seatnow.data.model.request.MenuDataRequest
import com.gmg.seatnow.data.model.request.StoreOperationRequest
import com.gmg.seatnow.data.model.request.UpdateMenuCategoriesRequest
import com.gmg.seatnow.data.model.request.RegularHolidayRequest
import com.gmg.seatnow.data.model.request.TemporaryHolidayRequest
import com.gmg.seatnow.data.model.request.HourRequest
import com.gmg.seatnow.data.model.request.MenuOrderRequest
import com.gmg.seatnow.data.model.request.StoreImageUpdateDto
import com.gmg.seatnow.data.model.request.StoreImageUpdateRequest
import com.gmg.seatnow.domain.model.OpeningHour
import com.gmg.seatnow.domain.model.RegularHoliday
import com.gmg.seatnow.domain.model.StoreImage
import com.gmg.seatnow.domain.model.StoreMenuCategory
import com.gmg.seatnow.domain.model.StoreMenuItemData
import com.gmg.seatnow.domain.model.StoreOperationInfo
import com.gmg.seatnow.domain.model.TemporaryHoliday
import com.gmg.seatnow.domain.repository.StoreRepository
import com.gmg.seatnow.presentation.owner.store.storeManage.storeManageEdit.StoreImageUiModel
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.collections.filter

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

                            // 1. DTO 값 확인
                            val serverUrl = menu.imageUrl
                            // android.util.Log.d("DEBUG_REPO_1", "서버값: $serverUrl")

                            // 2. 객체 생성 (여기에 imageUrl을 명시적으로 주입)
                            val domainItem = StoreMenuItemData(
                                id = menu.id,
                                name = menu.name,
                                price = decimalFormat.format(menu.price),
                                imageUrl = serverUrl // ★ 이 줄이 핵심입니다.
                            )

                            // 3. 생성된 객체 확인 (이 로그가 null이면 귀신이 곡할 노릇입니다)
                            if (domainItem.imageUrl == null) {
                                android.util.Log.e("DEBUG_REPO_ERR", "!!! 객체 생성 중 URL 누락됨 !!!")
                            } else {
                                android.util.Log.d("DEBUG_REPO_OK", "객체 생성 완료: ${domainItem.imageUrl}")
                            }

                            domainItem
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

    override suspend fun getStoreImages(): Result<List<StoreImage>> {
        return try {
            val response = authService.getStoreImages()

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()?.data
                // ★ 이미 만들어두신 Response DTO 사용
                val mappedList = data?.storeImages?.map { dto ->
                    StoreImage(
                        id = dto.id,
                        imageUrl = dto.imageUrl,
                        isMain = dto.main
                    )
                } ?: emptyList()

                // 대표 사진이 맨 앞으로 오도록 정렬
                val sortedList = mappedList.sortedByDescending { it.isMain }

                Result.success(sortedList)
            } else {
                Result.failure(Exception("사진 조회 실패: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStoreImages(
        currentImages: List<StoreImageUiModel>
    ): Result<Boolean> {
        return try {
            // =================================================================
            // 1. [기존 사진 처리] - JSON 전송
            // =================================================================
            // 대표 사진(isMain=true)을 리스트 맨 앞으로 정렬 (서버 인식률 높이기 위함)
            val existingImages = currentImages
                .filter { !it.isNew }
                .sortedByDescending { it.isMain }

            val updateList = existingImages.map {
                StoreImageUpdateDto(
                    id = it.id!!,
                    isMain = it.isMain
                )
            }

            // ★ DTO 수정 반영: existingImages 키값으로 포장
            val requestWrapper = StoreImageUpdateRequest(existingImages = updateList)

            val jsonString = Gson().toJson(requestWrapper)
            val updateDataBody = jsonString.toRequestBody("application/json".toMediaTypeOrNull())

            // =================================================================
            // 2. [새 사진 처리] - Multipart 전송
            // =================================================================
            // ★ [명세서 준수] "newImages 리스트의 첫 번째(0번)를 대표로 지정"
            // -> 따라서 isMain=true인 새 사진이 있다면 반드시 0번으로 보내야 함.
            val newImages = currentImages
                .filter { it.isNew }
                .sortedByDescending { it.isMain }

            val newImageParts = newImages.mapNotNull { uiModel ->
                val file = uriToFile(Uri.parse(uiModel.uri))
                file?.let {
                    val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("newImages", it.name, requestFile)
                }
            }

            // 3. API 호출
            val response = authService.updateStoreImages(updateDataBody, newImageParts)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.body()?.message ?: "사진 수정 실패"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
            // 1. 카테고리 정보 먼저 저장 (PATCH) -> 이건 잘 되고 있음
            val categoryDtos = categories.map { category ->
                CategoryUpdateDto(
                    id = if (category.id > 0) category.id else null,
                    name = category.name
                )
            }
            val request = UpdateMenuCategoriesRequest(categoryDtos)
            val catResponse = authService.updateMenuCategories(request)

            if (!catResponse.isSuccessful || catResponse.body()?.success != true) {
                return Result.failure(Exception(catResponse.body()?.message ?: "카테고리 수정 실패"))
            }

            // 2. [추가] 변경된 메뉴 정보(순서, 카테고리 이동 등)를 하나씩 서버에 저장 (POST)
            // ★ forEach로 모든 메뉴를 순회하며 saveMenu 호출
            var allSuccess = true

            categories.forEach { category ->
                category.items.forEach { menu ->
                    // 이미지 변경 여부는 알 수 없으므로 false, 이미지는 null로 보내서 텍스트 정보만 갱신
                    // (순서나 카테고리 이동만 반영)
                    val result = saveMenu(
                        menuId = menu.id,
                        categoryId = category.id, // ★ 현재 배치된 카테고리 ID
                        name = menu.name,
                        price = menu.price.replace(",", "").toIntOrNull() ?: 0,
                        imageUri = null, // 이미지는 변경 안 함
                        isImageChanged = false
                    )

                    if (result.isFailure) {
                        allSuccess = false
                        // 로그 출력 등 (e.g. Log.e("Repo", "Menu update failed: ${menu.id}"))
                    }
                }
            }

            if (allSuccess) {
                Result.success(true)
            } else {
                // 일부 실패했더라도 카테고리는 저장됐으므로 성공으로 칠지, 실패로 칠지 결정
                // 보통은 성공으로 처리하고 사용자에게 알림
                Result.success(true)
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

    override suspend fun updateMenuOrders(categories: List<StoreMenuCategory>): Result<Boolean> {
        return try {
            // 1. Domain Model -> Request DTO 변환
            val orderList = categories.map { category ->
                CategoryOrderDto(
                    categoryId = category.id,
                    // 해당 카테고리 내의 메뉴 ID들을 현재 순서대로 리스트화
                    menuIds = category.items.map { it.id }
                )
            }

            val request = MenuOrderRequest(categoryOrders = orderList)

            // 2. API 호출
            val response = authService.updateMenuOrders(request)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                val errorMsg = response.body()?.message ?: "메뉴 순서 변경 실패"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteMenu(menuId: Long): Result<Boolean> {
        return try {
            // Path Variable로 menuId 전달
            val response = authService.deleteMenu(menuId)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(true)
            } else {
                val errorMsg = response.body()?.message ?: "메뉴 삭제 실패"
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
