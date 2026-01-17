// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
// Android Application 플러그인
    alias(libs.plugins.android.application) apply false

    // Kotlin Android 플러그인
    alias(libs.plugins.kotlin.android) apply false

    // 👇 [핵심] Hilt 플러그인 추가 (버전은 2.51.1 추천)
    id("com.google.dagger.hilt.android") version "2.51.1" apply false

    // KSP (Hilt 컴파일러용)
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false

    // 5. 👇 [이게 빠져서 에러난 것] Serialization (Kotlin 버전과 똑같이 1.9.22로 맞춤)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22" apply false


}