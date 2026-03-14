package com.example.scriptaudio.data.local

/**
 * BookmarkDao
 *
 * Room 북마크 데이터 접근
 */

import androidx.room.*

import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Insert
    suspend fun insert(

        bookmark: BookmarkEntity

    )

    @Delete
    suspend fun delete(

        bookmark: BookmarkEntity

    )

    @Query("SELECT * FROM bookmarks")
    fun getAll(): Flow<List<BookmarkEntity>>

}