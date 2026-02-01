import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android) // Hilt 플러그인도 TOML에서 가져옴
    alias(libs.plugins.kotlin.serialization) // 직렬화 플러그인도 TOML에서 가져옴
    id("com.google.devtools.ksp") // KSP는 아직 별도 설정이 편할 수 있음
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.gmg.seatnow"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gmg.seatnow"
        minSdk = 29
        targetSdk = 35
        versionCode = 5
        versionName = "1.4"

        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 1. 네이버 지도 키 (이미 하셨다면 유지)
        val naverKey = localProperties.getProperty("NAVER_CLIENT_ID") ?: ""
        buildConfigField("String", "NAVER_CLIENT_ID", "\"$naverKey\"")

        // 2. [추가] 카카오 네이티브 앱 키 연결
        val kakaoKey = localProperties.getProperty("KAKAO_NATIVE_APP_KEY") ?: ""
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoKey\"")

        manifestPlaceholders["KAKAO_APP_KEY"] = kakaoKey
    }

    buildTypes {
        getByName("debug") {
            // ★ 디버그 모드에서는 반드시 false여야 합니다.
            isMinifyEnabled = false
            isShrinkResources = false
        }
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
        buildConfig = true // 필요시 사용
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
    implementation(libs.androidx.core.splashscreen) // TOML에 추가됨

    // 2. Jetpack Compose (BOM)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // 3. Navigation
    implementation(libs.androidx.navigation.compose)

    // 4. Hilt (DI)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler) // KSP 사용
    implementation(libs.androidx.hilt.navigation.compose)

    // 5. Network (Retrofit + Serialization + Gson)
    implementation(libs.retrofit)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.gson)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.serialization.converter)
    implementation(libs.gson)

    // 6. Coil
    implementation(libs.coil.compose)

    // 7. Third Party SDKs (Map, Kakao, Wheel)
    implementation(libs.naver.map.compose)
    implementation(libs.naver.map.sdk)
    implementation(libs.kakao.user)
    implementation(libs.wheel.picker)
    implementation(libs.play.services.location)

    // 8. DataStore
    implementation(libs.androidx.datastore.preferences)

    // 9. Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    implementation("androidx.multidex:multidex:2.0.1")
}