package com.example.scriptaudio.data.local

/**
 * ScriptAudio 저장 데이터
 */

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scripts")
data class ScriptEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,

    val path: String,

    val lastPage: Int = 0
)