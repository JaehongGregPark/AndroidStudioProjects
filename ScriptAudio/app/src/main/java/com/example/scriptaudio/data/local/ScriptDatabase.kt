package com.example.scriptaudio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Room Database 클래스
 *
 * 실제 SQLite DB 생성 관리
 *
 */
@Database(

    entities = [ScriptEntity::class],

    version = 1,

    exportSchema = false

)
abstract class ScriptDatabase : RoomDatabase() {

    /**
     * DAO 반환 함수
     *
     * Hilt에서 자동 주입됨
     */
    abstract fun scriptDao(): ScriptDao

}