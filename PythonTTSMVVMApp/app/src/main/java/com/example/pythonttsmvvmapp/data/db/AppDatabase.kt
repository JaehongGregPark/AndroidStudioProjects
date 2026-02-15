package com.example.pythonttsmvvmapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pythonttsmvvmapp.data.dao.ReadingDao
import com.example.pythonttsmvvmapp.data.entity.ReadingFile

/**
 * Room DB 정의
 */
@Database(
    entities = [ReadingFile::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun readingDao(): ReadingDao
}
