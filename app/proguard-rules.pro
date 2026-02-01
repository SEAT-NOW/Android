# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Retrofit 및 데이터 모델 강제 유지 (R8/ProGuard가 삭제하지 못하게 함)
-keep class com.gmg.seatnow.data.model.** { *; }
-keepclassmembers class com.gmg.seatnow.data.model.** { *; }

# Gson 관련 규칙 (직렬화/역직렬화 시 필요)
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

# --- Kotlin Serialization 필수 규칙 (이게 없으면 R8이 돕니다) ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationException
-keepclassmembers class kotlinx.serialization.json.internal.JsonTreeReader {
    *** readDeepRecursive(...);
}

# DTO들은 이름과 필드를 절대 건드리지 마라
-keep class com.gmg.seatnow.data.model.** { *; }
-keepclassmembers class com.gmg.seatnow.data.model.** { *; }

# Serialization 관련 클래스 유지
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.** { *; }
-keep interface kotlinx.serialization.** { *; }