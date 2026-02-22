package com.example.scriptaudio.data.local

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository
 *
 * ViewModel → Repository → DAO
 */
@Singleton
class ScriptRepository @Inject constructor(

    private val dao: ScriptDao

) {

    suspend fun insert(script: ScriptEntity) {

        dao.insert(script)

    }

}