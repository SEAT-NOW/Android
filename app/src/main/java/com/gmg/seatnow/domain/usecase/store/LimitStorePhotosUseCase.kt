package com.gmg.seatnow.domain.usecase.store

import android.net.Uri
import javax.inject.Inject

class LimitStorePhotosUseCase @Inject constructor() {
    companion object {
        const val MAX_PHOTO_COUNT = 5 // ★ 정책 변경 시 여기만 10으로 바꾸면 끝!
    }

    operator fun invoke(currentList: List<Uri>, newUris: List<Uri>): List<Uri> {
        return (currentList + newUris).distinct().take(MAX_PHOTO_COUNT)
    }
}