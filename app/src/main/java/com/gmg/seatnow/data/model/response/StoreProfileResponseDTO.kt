package com.gmg.seatnow.data.model.response

data class StoreProfileResponseDTO(
    val representativeName: String,
    val businessNumber: String,
    val storeName: String,
    val address: String,
    val universityNames: List<String>?, // 리스트 형태
    val businessLicenseFileName: String?,
    val storePhone: String?
)