package com.example.audioscript.data.di

import com.example.audioscript.data.repository.TextRepositoryImpl
import com.example.audioscript.domain.repository.TextRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindTextRepository(
        impl: TextRepositoryImpl
    ): TextRepository
}
