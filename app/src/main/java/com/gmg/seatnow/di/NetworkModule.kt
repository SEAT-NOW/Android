package com.gmg.seatnow.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import javax.inject.Singleton
import com.gmg.seatnow.data.local.AuthManager
import com.gmg.seatnow.data.api.AuthInterceptor
import com.gmg.seatnow.data.api.AuthService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import javax.inject.Provider

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "https://seatnow.r-e.kr/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true // DTO에 없는 필드 건너뛰기 (Lenient 역할)
        coerceInputValues = true // null 안전 처리
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authManager: AuthManager,
        authServiceProvider: Provider<AuthService>
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val authInterceptor = AuthInterceptor(authManager, authServiceProvider)

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json // 위에서 만든 Json 주입
    ): Retrofit {
        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            // ★ 여기가 핵심 변경 포인트! (GsonConverterFactory 삭제됨)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}