    package com.gmg.seatnow.data.model.response

import android.annotation.SuppressLint
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class StoreImageResponse(
    @SerializedName("storeImages") val storeImages: List<StoreImageDTO>
)

@SuppressLint("UnsafeOptInUsageError")
@Keep
@Serializable
data class StoreImageDTO(
    @SerializedName("id") val id: Long,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("main") val main: Boolean
)