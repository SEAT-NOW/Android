package com.gmg.seatnow.presentation.owner.store

import androidx.annotation.DrawableRes
import com.gmg.seatnow.R

enum class StoreTab (
    val title: String,
    @DrawableRes val iconResId: Int
) {
    SEAT_MANAGEMENT("좌석 관리", R.drawable.ic_chair),
    MY_PAGE("마이페이지", R.drawable.ic_mypage)
}