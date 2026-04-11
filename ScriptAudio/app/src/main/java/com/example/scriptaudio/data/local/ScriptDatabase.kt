package com.example.scriptaudio.data.local

/**
 * Room Database
 */

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ScriptEntity::class],
    version = 1
)
abstract class ScriptDatabase : RoomDatabase() {

    abstract fun scriptDao(): ScriptDao
}