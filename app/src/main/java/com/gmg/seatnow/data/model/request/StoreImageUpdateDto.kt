package com.gmg.seatnow.data.model.request

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

// 개별 사진 정보
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class StoreImageUpdateDto(
    @SerializedName("id") val id: Long,
    @SerializedName("isMain") val isMain: Boolean
)

// ★ [핵심] 리스트를 감싸는 껍데기 (이게 있어야 JSON Object가 됨)
@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class StoreImageUpdateRequest(
    // 백엔드가 이 리스트의 변수명을 무엇으로 받는지 확인 필요하지만,
    // 보통 'updateImages' 혹은 'storeImages' 등을 씁니다. 
    // 일단 사용자님 기존 코드대로 'updateImages'로 지정합니다.
    @SerializedName("existingImages") val existingImages: List<StoreImageUpdateDto>
)