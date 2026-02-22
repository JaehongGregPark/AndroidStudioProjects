package com.example.scriptaudio.data.local

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface ScriptDao {

    @Insert
    suspend fun insert(script: ScriptEntity)

}