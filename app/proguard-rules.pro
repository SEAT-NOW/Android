# ==========================================================
# 1. [생명줄] 제네릭(<T>), 어노테이션, 라인 넘버 유지
# ==========================================================
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes SourceFile,LineNumberTable
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations

# ★ [추가된 핵심] Kotlin Metadata 정보 유지 (Retrofit이 코루틴 분석할 때 필수)
-keep class kotlin.Metadata { *; }

# ==========================================================
# 2. 내 앱의 데이터 & API 보호
# ==========================================================
# DTO와 BaseResponse 같은 래퍼 클래스 보호
-keep class com.gmg.seatnow.data.model.** { *; }
-keepclassmembers class com.gmg.seatnow.data.model.** { *; }

# API 인터페이스 보호
-keep class com.gmg.seatnow.data.api.** { *; }
-keep interface com.gmg.seatnow.data.api.** { *; }
-keepclassmembers interface com.gmg.seatnow.data.api.** {
    @retrofit2.http.* <methods>;
}

# ==========================================================
# 3. 라이브러리 규칙 (Retrofit, OkHttp)
# ==========================================================
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ==========================================================
# 4. [범인 검거] Coroutines & Suspend 함수 강력 보호
# Unable to create call adapter 에러의 주원인을 막습니다.
# ==========================================================
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }
-keep class kotlin.coroutines.** { *; }
-keep class kotlin.coroutines.jvm.internal.** { *; }
-keep class kotlinx.coroutines.android.** { *; }

# Suspend 함수가 컴파일되면 생기는 Continuation 객체 보호
-keepclassmembers class * {
    @org.jetbrains.annotations.Nullable <methods>;
    @org.jetbrains.annotations.NotNull <methods>;
    private kotlin.coroutines.Continuation *;
}

# ==========================================================
# 5. Serialization & Gson
# ==========================================================
-dontwarn kotlinx.serialization.**
-keep class kotlinx.serialization.** { *; }
-keep interface kotlinx.serialization.** { *; }
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationException

-keepclassmembers class kotlinx.serialization.json.internal.JsonTreeReader {
    *** readDeepRecursive(...);
}

-keep class com.google.gson.** { *; }

# ==========================================================
# 6. Kakao SDK
# ==========================================================
-keep class com.kakao.sdk.** { *; }
-keep interface com.kakao.sdk.** { *; }
-dontwarn com.kakao.sdk.**