import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.devtools.ksp")
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.gmg.seatnow"
    compileSdk = 35
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
    defaultConfig {
        applicationId = "com.gmg.seatnow"
        minSdk = 29
        targetSdk = 35
        val runNumber = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 0
        versionCode = 15 + runNumber
        versionName = "2.8.$runNumber"

        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val naverKey = localProperties.getProperty("NAVER_CLIENT_ID") ?: ""
        buildConfigField("String", "NAVER_CLIENT_ID", "\"$naverKey\"")

        val kakaoKey = localProperties.getProperty("KAKAO_NATIVE_APP_KEY") ?: ""
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoKey\"")

        val baseUrl = (localProperties.getProperty("BASE_URL") ?: "http://localhost/").trim()
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")

        manifestPlaceholders["KAKAO_APP_KEY"] = kakaoKey
    }

    signingConfigs {
        create("release") {
            storeFile = file(localProperties.getProperty("STORE_FILE") ?: "keystore.jks")
            storePassword = localProperties.getProperty("STORE_PASSWORD")
            keyAlias = localProperties.getProperty("KEY_ALIAS")
            keyPassword = localProperties.getProperty("KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            isCrunchPngs = false
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true

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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
}

// ❌ android { ... } 내부에 있던 잘못된 ksp 블록은 제거하고
// ⭕ 최하단에 에러 타입 보정 옵션만 깔끔하게 하나로 통일합니다.
ksp {
    arg("correctErrorTypes", "true")
}

hilt {
    enableAggregatingTask = true
}

dependencies {
    // 1. Android Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)

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
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // 5. Network (Retrofit + Serialization + Gson)
    implementation(libs.retrofit)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.serialization.converter)

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

// KSP가 생성된 BuildConfig 코드를 정상적으로 순서 보장하여 읽도록 태스크 의존성 명시
project.afterEvaluate {
    tasks.withType<com.google.devtools.ksp.gradle.KspTaskJvm> {
        dependsOn(tasks.withType<com.android.build.gradle.tasks.GenerateBuildConfig>())
    }
}
