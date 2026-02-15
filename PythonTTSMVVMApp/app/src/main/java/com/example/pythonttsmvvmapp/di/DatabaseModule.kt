package com.example.pythonttsmvvmapp.di

import android.content.Context
import androidx.room.Room
import com.example.pythonttsmvvmapp.data.dao.ReadingDao
import com.example.pythonttsmvvmapp.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI 등록
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "reading_db"
        ).build()
    }

    @Provides
    fun provideDao(
        db: AppDatabase
    ): ReadingDao = db.readingDao()
}