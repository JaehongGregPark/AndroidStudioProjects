package com.example.scriptaudio.di

import android.content.Context
import androidx.room.Room

import com.example.scriptaudio.data.local.*

import com.example.scriptaudio.tts.TTSManager

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn

import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Room DB 제공
     */
    @Provides
    @Singleton
    fun provideDatabase(

        @ApplicationContext context: Context

    ): ScriptDatabase {

        return Room.databaseBuilder(

            context,
            ScriptDatabase::class.java,
            "script_db"

        ).build()

    }


    /**
     * Repository 제공
     */
    @Provides
    @Singleton
    fun provideRepository(

        db: ScriptDatabase

    ): ScriptRepository {

        return ScriptRepositoryImpl(

            db.scriptDao()

        )

    }


    /**
     * TTS 제공
     */
    @Provides
    @Singleton
    fun provideTTS(

        @ApplicationContext context: Context

    ): TTSManager {

        return TTSManager(context)

    }

}