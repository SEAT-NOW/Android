package com.gmg.seatnow.domain.model

enum class StoreStatus {
    SPARE,  // 한적 (노랑)
    NORMAL, // 보통 (노랑)
    HARD,   // 혼잡 (빨강)
    FULL    // 만석 (회색)
}

data class Store(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val status: StoreStatus = StoreStatus.NORMAL, // 상태 추가
    val imageUrl: String? = null,
    val neighborhood: String,
    val images: List<String>,
    val distance: String,
    val operationStatus: String,
    val storePhone: String?,
    val availableSeatCount: Int = 0,
    val totalSeatCount: Int = 0
)