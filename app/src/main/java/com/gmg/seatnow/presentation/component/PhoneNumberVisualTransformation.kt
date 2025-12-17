package com.gmg.seatnow.presentation.component

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PhoneNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // 숫자만 남기기
        val trimmed = if (text.text.length >= 13) text.text.substring(0..12) else text.text
        var out = ""

        // 하이픈(-) 추가 로직 (010-0000-0000 포맷)
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 2 || i == 6) {
                if (i < trimmed.lastIndex) out += "-"
            }
        }

        val phoneNumberOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 7) return offset + 1
                return offset + 2
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 9) return offset - 1
                return offset - 2
            }
        }

        return TransformedText(AnnotatedString(out), phoneNumberOffsetTranslator)
    }
}