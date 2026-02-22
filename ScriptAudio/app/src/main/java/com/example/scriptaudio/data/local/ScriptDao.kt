package com.example.scriptaudio.data.local

import androidx.room.Dao
import androidx.room.Insert

/**
 * DAO = Database Access Object
 *
 * Database 접근 인터페이스
 *
 * Room은 이 인터페이스를 자동 구현함
 *
 */
@Dao
interface ScriptDao {

    /**
     * DB Insert 함수
     *
     * suspend
     *
     * → Coroutine에서 실행해야함
     *
     */
    @Insert
    suspend fun insert(script: ScriptEntity)

}