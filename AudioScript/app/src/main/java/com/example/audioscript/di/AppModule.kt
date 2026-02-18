package com.example.audioscript.di

import com.example.audioscript.data.repository.TextRepositoryImpl
import com.example.audioscript.domain.repository.TextRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt 의존성 주입 모듈
 *
 * Repository 구현체를
 * Domain 인터페이스에 바인딩한다.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {


}
