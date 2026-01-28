package com.gmg.seatnow.domain.model

data class StoreDetail(
    val id: Long,
    val name: String,
    val images: List<String>,             // "Image list"
    val operationStatus: String,          // "영업 상태" (예: "영업 중")
    val storePhone: String,
    val availableSeatCount: Int,          // "허용 가능 좌석"
    val totalSeatCount: Int,              // "토탈 좌석 수"
    val status: StoreStatus,              // "status(태그에 사용할)"
    val universityInfo: String,           // "근처 대학 정보"
    val address: String,                  // "가게 상세 주소"
    val openHours: String,                // "영업 시간"
    val closedDays: String,               // "휴무일"
    val isKept: Boolean = false
)

data class MenuCategoryUiModel(
    val categoryName: String,
    val menuItems: List<MenuItemUiModel>
)

data class MenuItemUiModel(
    val id: Long,
    val name: String,
    val price: Int,
    val imageUrl: String,
    val isRecommended: Boolean,
    val isLiked: Boolean // 사용자가 이 메뉴에 '따봉(좋아요)'를 눌렀는지 여부
)