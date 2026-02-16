package com.example.pythonttsmvvmapp.di

import com.example.pythonttsmvvmapp.reader.data.repository.ReaderRepository
import com.example.pythonttsmvvmapp.reader.data.repository.ReaderRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindReaderRepository(
        impl: ReaderRepositoryImpl
    ): ReaderRepository
}
