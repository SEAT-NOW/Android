package com.gmg.seatnow.di

import com.gmg.seatnow.data.repository.AuthRepositoryImpl
import com.gmg.seatnow.data.repository.ImageRepository
import com.gmg.seatnow.data.repository.MockImageRepositoryImpl
import com.gmg.seatnow.domain.repository.AuthRepository
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
}