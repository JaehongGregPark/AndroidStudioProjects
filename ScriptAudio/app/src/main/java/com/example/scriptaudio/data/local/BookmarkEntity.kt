package com.example.scriptaudio.data.local

/**
 * BookmarkEntity
 *
 * Room DB 북마크 테이블
 */

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val filePath: String,

    val page: Int,

    val note: String = ""

)