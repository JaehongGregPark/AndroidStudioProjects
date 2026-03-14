package com.example.scriptaudio.data.repository

import com.example.scriptaudio.data.local.ScriptDao
import com.example.scriptaudio.data.local.ScriptEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository 실제 구현
 */

@Singleton
class ScriptRepositoryImpl @Inject constructor(

    private val scriptDao: ScriptDao

) : ScriptRepository {

    override suspend fun getAllScripts(): List<ScriptEntity> {
        return scriptDao.getAll()
    }

    override suspend fun insertScript(script: ScriptEntity) {
        scriptDao.insert(script)
    }
}