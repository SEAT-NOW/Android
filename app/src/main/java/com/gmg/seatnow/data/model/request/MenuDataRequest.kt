package com.gmg.seatnow.data.model.request

import com.google.gson.annotations.SerializedName

data class MenuDataRequest(
    @SerializedName("id")
    val id: Long?, // 신규는 null, 수정은 ID
    @SerializedName("name")
    val name: String,
    @SerializedName("price")
    val price: Int,
    @SerializedName("categoryId")
    val categoryId: Long,
    @SerializedName("keepImage")
    val keepImage: Boolean // 기존 이미지 유지 여부
)