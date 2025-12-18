package com.gmg.seatnow.presentation.component

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

// 1. 사업자 등록번호 (000-00-00000)
class BusinessNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 10) text.text.substring(0..9) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 2 || i == 4) { // 3자리 뒤, 2자리 뒤(총 5자리 뒤)
                if (i < trimmed.lastIndex) out += "-"
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 5) return offset + 1
                return offset + 2
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 7) return offset - 1
                return offset - 2
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

// 2. 가게 유선 연락처 (02 케이스 처리 포함)
class NumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val isSeoul = original.startsWith("02")

        // 02인 경우 최대 10자리, 그 외 최대 11자리 제한
        val maxLength = if (isSeoul) 10 else 11
        val trimmed = if (original.length > maxLength) original.substring(0 until maxLength) else original

        var out = ""
        // 서울(02) 로직
        if (isSeoul) {
            // 9자리 이하 (02-XXX-XXXX), 10자리 (02-XXXX-XXXX)
            val isTen = trimmed.length == 10
            for (i in trimmed.indices) {
                out += trimmed[i]
                // 첫번째 하이픈: 02 뒤
                if (i == 1 && i < trimmed.lastIndex) out += "-"
                // 두번째 하이픈: 9자리일 땐 5번째(idx 4) 뒤, 10자리일 땐 6번째(idx 5) 뒤
                if ((isTen && i == 5) || (!isTen && i == 4)) {
                    if (i < trimmed.lastIndex) out += "-"
                }
            }
        } else {
            // 그 외 (0XX-XXX-XXXX 또는 0XX-XXXX-XXXX)
            val isEleven = trimmed.length == 11
            for (i in trimmed.indices) {
                out += trimmed[i]
                // 첫번째 하이픈: 3자리 뒤
                if (i == 2 && i < trimmed.lastIndex) out += "-"
                // 두번째 하이픈: 10자리일 땐 6번째(idx 5) 뒤, 11자리일 땐 7번째(idx 6) 뒤
                if ((isEleven && i == 6) || (!isEleven && i == 5)) {
                    if (i < trimmed.lastIndex) out += "-"
                }
            }
        }

        // Offset 매핑 (단순화를 위해 기본적인 길이 기반 매핑 사용)
        // 복잡한 하이픈 위치 변화로 인해 완벽한 커서 위치 보정을 위해선 별도 계산이 필요하나,
        // 입력 편의상 Transformed 텍스트 길이에 맞춰 끝으로 이동하거나 비례 이동하도록 처리
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // 대략적인 매핑 (정확도 향상을 위해선 위 로직과 동일한 조건문 필요)
                // 여기서는 입력값 보호를 위해 단순 계산 로직 적용
                val textLen = trimmed.length
                val outLen = out.length
                if (offset >= textLen) return outLen

                // 실제 위치 계산
                var transformedOffset = 0
                var originCount = 0
                for (char in out) {
                    if (originCount == offset) break
                    if (char != '-') originCount++
                    transformedOffset++
                }
                return transformedOffset
            }

            override fun transformedToOriginal(offset: Int): Int {
                val outLen = out.length
                if (offset >= outLen) return trimmed.length

                var originOffset = 0
                for (i in 0 until offset) {
                    if (out[i] != '-') originOffset++
                }
                return originOffset
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}