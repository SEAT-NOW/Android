package com.gmg.seatnow.data.model.request

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class UpdateMenuCategoriesRequest(
    @SerializedName("categories")
    val categories: List<CategoryUpdateDto>
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class CategoryUpdateDto(
    @SerializedName("id")
    val id: Long?, // 신규 추가는 null, 수정은 기존 ID
    @SerializedName("name")
    val name: String
)