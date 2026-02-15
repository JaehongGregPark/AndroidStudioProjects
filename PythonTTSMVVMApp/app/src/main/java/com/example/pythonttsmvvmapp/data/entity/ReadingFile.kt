package com.example.pythonttsmvvmapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 음성 파일 저장 테이블
 */
@Entity(tableName = "reading_files")
data class ReadingFile(

    @PrimaryKey
    val name: String,

    val path: String
)