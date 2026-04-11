package com.example.scriptaudio.data.local

/**
 * Room DAO
 * ScriptAudio 데이터베이스 접근 계층
 */

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScriptDao {

    /**
     * 스크립트 저장
     */
    @Insert
    suspend fun insert(script: ScriptEntity)

    /**
     * 모든 스크립트 조회
     */
    @Query("SELECT * FROM scripts")
    suspend fun getAll(): List<ScriptEntity>

}