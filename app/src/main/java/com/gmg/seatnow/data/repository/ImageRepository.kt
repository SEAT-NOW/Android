package com.gmg.seatnow.data.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.gmg.seatnow.data.api.FileApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

interface ImageRepository {
    suspend fun uploadImage(uri: Uri): Result<String>
}

@Singleton
class ImageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: FileApiService
) : ImageRepository {

    override suspend fun uploadImage(uri: Uri): Result<String> {
        return runCatching {
            // 1. Uri -> 임시 파일(File)로 변환
            val file = uriToFile(uri) ?: throw Exception("파일 변환 실패")

            // 2. RequestBody 및 Multipart 생성
            // 이미지 타입 추론 (jpeg, png 등)
            val type = context.contentResolver.getType(uri) ?: "image/*"
            val requestFile = file.asRequestBody(type.toMediaTypeOrNull())

            // "image"는 서버에서 받는 파라미터 키 값 (백엔드와 협의 필요)
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

            // 3. API 호출
            val response = apiService.uploadImage(body)

            // 4. 결과 URL 반환
            response.imageUrl
        }
    }

    // Uri 내용을 읽어서 임시 파일(Cache Dir)로 복사하는 헬퍼 함수
    private fun uriToFile(uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null

            // 파일 확장자 추론
            val mimeType = contentResolver.getType(uri)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"

            val tempFile = File.createTempFile("upload_", ".$extension", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}