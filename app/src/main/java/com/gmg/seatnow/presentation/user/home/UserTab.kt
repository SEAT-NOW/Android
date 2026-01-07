package com.gmg.seatnow.presentation.user.home

import androidx.annotation.DrawableRes
import com.gmg.seatnow.R

enum class UserTab(
    val title: String,
    @DrawableRes val iconResId: Int
) {
    // ⚠️ 주의: R.drawable.ic_home 리소스가 있어야 합니다. 없으면 추가해주세요!
    HOME("홈", R.drawable.ic_seatnow_home),
    SEAT_SEARCH("N명 자리찾기", R.drawable.ic_n_person)
}