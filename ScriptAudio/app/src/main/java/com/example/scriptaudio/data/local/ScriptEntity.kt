package com.example.scriptaudio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Script 테이블
 */
@Entity(tableName = "script")
data class ScriptEntity(

    /**
     * 자동 증가 ID
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val text: String

)