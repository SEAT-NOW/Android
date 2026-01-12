package com.gmg.seatnow.di

import com.gmg.seatnow.data.repository.AuthRepositoryImpl
import com.gmg.seatnow.data.repository.ImageRepository
import com.gmg.seatnow.data.repository.MockImageRepositoryImpl
import com.gmg.seatnow.data.repository.MockMapRepositoryImpl
import com.gmg.seatnow.data.repository.SeatRepositoryImpl
import com.gmg.seatnow.domain.repository.AuthRepository
import com.gmg.seatnow.domain.repository.SeatRepository
import com.gmg.seatnow.domain.repository.MapRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindImageRepository(
        mockRepository: MockImageRepositoryImpl
    ): ImageRepository

    /*
    @Binds
    @Singleton
    abstract fun bindImageRepository(
        realRepository: ImageRepositoryImpl
    ): ImageRepository
    */

    @Binds
    @Singleton
    abstract fun bindSeatRepository(
        seatRepositoryImpl: SeatRepositoryImpl
    ): SeatRepository

    @Binds
    @Singleton
    abstract fun bindStoreRepository(
        mockRepositoryImpl: MockMapRepositoryImpl
    ): MapRepository
}