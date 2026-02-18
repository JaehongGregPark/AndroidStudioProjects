package com.example.audioscript.di

import com.example.audioscript.data.repository.TextRepositoryImpl
import com.example.audioscript.domain.repository.TextRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * ===============================
 * Dependency Injection Module
 * ===============================
 *
 * 역할:
 *  - 인터페이스와 구현체 연결
 *
 * Clean Architecture 핵심:
 *  - Domain은 구현을 모른다.
 *  - DI가 구현을 연결해준다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindTextRepository(
        impl: TextRepositoryImpl
    ): TextRepository
}
