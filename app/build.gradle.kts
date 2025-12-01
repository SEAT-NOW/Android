plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    // 👇 아키텍처를 위해 추가해야 할 플러그인들
    id("com.google.dagger.hilt.android") // Hilt
    id("com.google.devtools.ksp") // KSP (Hilt용)
    id("org.jetbrains.kotlin.plugin.serialization") // JSON 처리
}

android {
    namespace = "com.example.a4th_mainproject_seatnow_android"
    compileSdk = 35 // 에러 해결을 위해 35 유지

    defaultConfig {
        applicationId = "com.example.a4th_mainproject_seatnow_android"
        minSdk = 29 // 29면 Android 10 이상. 적절합니다.
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        // 👇 여기를 "1.3.2"가 아니라 "1.5.10"으로 고치세요!
        kotlinCompilerExtensionVersion = "1.5.10"
    }}

dependencies {
    // --- 기본 Android ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // 👇 --- [추가] 협업/아키텍처 필수 라이브러리 --- 👇

    // 1. Navigation (화면 이동)
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // 2. Hilt (의존성 주입) - libs.hilt... 가 toml에 없다면 아래처럼 직접 버전을 적어도 됩니다
    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // 3. Network (Retrofit + OkHttp) - 서버 통신
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // 4. Image Loading (Coil)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // --- 테스트 ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}