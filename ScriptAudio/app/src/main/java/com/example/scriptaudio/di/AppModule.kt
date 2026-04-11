package com.example.scriptaudio.di

/**
 * Hilt Dependency Injection Module
 *
 * 앱 전체에서 사용하는 Singleton 객체 제공
 */

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.room.Room
import com.example.scriptaudio.data.local.ScriptDatabase
import com.example.scriptaudio.data.repository.ScriptRepository
import com.example.scriptaudio.data.repository.ScriptRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Room Database 생성
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
     *
     * ScriptRepository → ScriptRepositoryImpl 매핑
     */
    @Provides
    @Singleton
    fun provideRepository(
        database: ScriptDatabase
    ): ScriptRepository {

        return ScriptRepositoryImpl(
            database.scriptDao()
        )

    }

    /**
     * TextToSpeech Singleton 제공
     */
    @Provides
    @Singleton
    fun provideTTS(
        @ApplicationContext context: Context
    ): TextToSpeech {

        val tts = TextToSpeech(context, null)

        tts.language = Locale.US

        return tts

    }

}