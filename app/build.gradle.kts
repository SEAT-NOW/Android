plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // 👇 직렬화(JSON) 및 Hilt 설정
    id("kotlin-kapt") // ksp 사용시 제거 가능하나, 안전을 위해 유지하거나 ksp로 완전 전환 권장
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.seatnow" // 👈 수정됨
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.seatnow" // 👈 수정됨
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}

dependencies {
    // 1. Android Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // 👇 스플래시 API (Android 12 이상 필수 대응)
    implementation("androidx.core:core-splashscreen:1.0.1")

    // 2. Jetpack Compose (BOM 사용)
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // 3. Navigation
    implementation(libs.androidx.navigation.compose)

    // 4. Hilt (Dependency Injection) - KSP 사용
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // 5. Network (Retrofit + Kotlinx Serialization)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // 6. Coil (Image Loading)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // 7. Third Party SDKs
    implementation("io.github.fornewid:naver-map-compose:1.7.2")
    implementation("com.naver.maps:map-sdk:3.19.0")
    implementation("com.kakao.sdk:v2-user:2.19.0")
}