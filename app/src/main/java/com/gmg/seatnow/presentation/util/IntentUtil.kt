package com.gmg.seatnow.presentation.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

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
}