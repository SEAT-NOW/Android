package com.gmg.seatnow.domain.usecase.logic

import javax.inject.Inject

class ExtractNeighborhoodUseCase @Inject constructor() {
    operator fun invoke(address: String): String {
        val split = address.split(" ")
        // "동", "읍", "면"으로 끝나는 단어 찾기, 없으면 "정보 없음" 반환
        return split.find { it.endsWith("동") || it.endsWith("읍") || it.endsWith("면") } 
            ?: "정보 없음"
    }
}