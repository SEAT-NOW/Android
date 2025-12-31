package com.gmg.seatnow.presentation.owner.signup

enum class SignUpStep(
    val title: String, 
    val progress: Float
) {
    // 1단계: 기본 정보 (20%)
    STEP_1_BASIC("사장님 회원가입", 0.2f),
    
    // 2단계: 사업자 정보 (40%)
    STEP_2_BUSINESS("사업자 정보 입력", 0.4f),
    
    // 3단계: 공간 테이블 구성 입력 (60%)
    STEP_3_STORE("공간/테이블 구성 입력", 0.6f),

    // 4단계: 운영 정보 입력 (80%)
    STEP_4_OPERATION("운영 정보 입력", 0.8f),
    
    // 5단계: 가게 사진 등록 (100%)
    STEP_5_PHOTO("가게 사진 등록", 1f),

    // 완료단계 : 회원가입 완료(100%)
    STEP_6_COMPLETE("회원가입 완료", 1f)
}