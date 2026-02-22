package com.example.scriptaudio.di

import android.content.Context
import androidx.room.Room
import com.example.scriptaudio.data.local.ScriptDao
import com.example.scriptaudio.data.local.ScriptDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
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
     * DAO 제공
     */
    @Provides
    fun provideDao(

        db: ScriptDatabase

    ): ScriptDao {

        return db.scriptDao()

    }

}