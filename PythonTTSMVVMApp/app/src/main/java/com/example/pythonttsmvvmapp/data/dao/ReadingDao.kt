package com.example.pythonttsmvvmapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pythonttsmvvmapp.data.entity.ReadingFile

@Dao
interface ReadingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(file: ReadingFile)

    @Query("SELECT * FROM reading_files WHERE name = :name")
    suspend fun get(name: String): ReadingFile?
}