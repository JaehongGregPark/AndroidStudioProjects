package com.example.scriptaudio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(

    entities = [ScriptEntity::class],
    version = 1,
    exportSchema = false

)
abstract class ScriptDatabase : RoomDatabase() {

    abstract fun scriptDao(): ScriptDao

}