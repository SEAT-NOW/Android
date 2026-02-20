package com.gmg.seatnow.data.model.response

import android.annotation.SuppressLint
import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class StoreKeptResponseDTO(
    @SerialName("storeId") val storeId: Long = 0L,
    @SerialName("storeName") val storeName: String = "",

    // ★ [수정] 서버가 리스트로 보내므로 List<String>으로 변경해야 함!
    @SerialName("universityNames") val universityNames: List<String>? = emptyList(),

    @SerialName("statusTagName") val statusTagName: String? = "",
    @SerialName("totalSeatCount") val totalSeatCount: Int = 0,
    @SerialName("usedSeatCount") val usedSeatCount: Int = 0,
    @SerialName("images") val images: String? = null // 로그 보니 이미지는 String 하나로 오네요
)