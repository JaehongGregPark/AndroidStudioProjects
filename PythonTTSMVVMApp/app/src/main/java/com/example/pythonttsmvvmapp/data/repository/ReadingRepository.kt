package com.example.pythonttsmvvmapp.data.repository

import com.example.pythonttsmvvmapp.data.dao.ReadingDao
import com.example.pythonttsmvvmapp.data.entity.ReadingFile
import javax.inject.Inject

class ReadingRepository @Inject constructor(
    private val dao: ReadingDao
) {
    suspend fun save(file: ReadingFile) = dao.save(file)
    suspend fun get(name: String) = dao.get(name)
}
