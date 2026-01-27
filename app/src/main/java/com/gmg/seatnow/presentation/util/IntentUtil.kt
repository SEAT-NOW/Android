package com.gmg.seatnow.presentation.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.text.DecimalFormat

object IntentUtil {
    fun makePhoneCall(context: Context, phoneNumber: String?) {
        if (phoneNumber.isNullOrBlank()) {
            Toast.makeText(context, "등록된 전화번호가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 안드로이드 기본 전화 다이얼러 앱으로 번호만 입력된 상태로 이동 (통화 버튼은 사용자가 누름)
        // [장점] CALL_PHONE 권한이 필요 없어 플레이스토어 심사 시 리젝(거절) 사유가 되지 않음
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        context.startActivity(intent)
    }

    fun formatPrice(price: Int): String {
        return DecimalFormat("#,###").format(price) + "원"
    }

    fun shareStoreLink(context: Context, storeId: Long, storeName: String) {
        // 1. 공유할 링크 생성 (예: https://seatnow.r-e.kr/store/123)
        // 실제 운영할 도메인 주소를 사용합니다.
        val shareLink = "seatnow://seatnow.r-e.kr/store/$storeId"

        // 2. 카카오톡/문자 등에 보낼 메시지 구성
        val shareMessage = "[SeatNow] 지금 '$storeName'의 빈 좌석을 확인해보세요!\n$shareLink"

        // 3. 안드로이드 기본 공유 인텐트 생성
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
        }

        // 4. 시스템 공유 시트 띄우기
        val shareIntent = Intent.createChooser(sendIntent, "가게 공유하기")
        context.startActivity(shareIntent)
    }
}