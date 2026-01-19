package com.gmg.seatnow.di

import com.gmg.seatnow.data.api.AuthService
import com.gmg.seatnow.data.api.UserApiService
import com.gmg.seatnow.data.repository.AuthRepositoryImpl
import com.gmg.seatnow.data.repository.MapRepositoryImpl
import com.gmg.seatnow.data.repository.SeatRepositoryImpl
import com.gmg.seatnow.domain.repository.AuthRepository
import com.gmg.seatnow.domain.repository.MapRepository
import com.gmg.seatnow.domain.repository.SeatRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    // =================================================================
    // 1. Repository 구현체 연결 (@Binds)
    // - 인터페이스와 구현체를 연결해줍니다.
    // =================================================================

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSeatRepository(
        seatRepositoryImpl: SeatRepositoryImpl
    ): SeatRepository

    @Binds
    @Singleton
    abstract fun bindMapRepository(
        mapRepositoryImpl: MapRepositoryImpl
    ): MapRepository

    // =================================================================
    // 2. Retrofit Service 생성 (@Provides)
    // - abstract class 안에서는 반드시 'companion object' 안에 넣어야 합니다.
    // =================================================================

    companion object {

        // [기존] 인증 관련 API 서비스 제공
        @Provides
        @Singleton
        fun provideAuthService(retrofit: Retrofit): AuthService {
            return retrofit.create(AuthService::class.java)
        }

        // [신규] 지도/가게 관련 API 서비스 제공
        // (사용자님 코드의 UserApiService 대신 아까 만든 StoreApiService 사용)
        @Provides
        @Singleton
        fun provideStoreApiService(retrofit: Retrofit): UserApiService {
            return retrofit.create(UserApiService::class.java)
        }
    }
}