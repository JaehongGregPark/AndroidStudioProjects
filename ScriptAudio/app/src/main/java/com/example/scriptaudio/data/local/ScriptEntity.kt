package com.example.scriptaudio.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database 에 저장되는 테이블 클래스
 *
 * Entity = SQLite 테이블이라고 생각하면 됨
 *
 * 테이블 이름 = ScriptEntity
 *
 */
@Entity
data class ScriptEntity(

    /**
     * Primary Key (기본 키)
     *
     * autoGenerate = true
     *
     * → Room이 자동으로 번호 생성
     *
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,


    /**
     * 저장할 실제 텍스트
     */
    val text: String

)