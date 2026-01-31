package com.gmg.seatnow.data.model.request

import com.google.gson.annotations.SerializedName

data class UpdateMenuCategoriesRequest(
    @SerializedName("categories")
    val categories: List<CategoryUpdateDto>
)

data class CategoryUpdateDto(
    @SerializedName("id")
    val id: Long?, // 신규 추가는 null, 수정은 기존 ID
    @SerializedName("name")
    val name: String
)