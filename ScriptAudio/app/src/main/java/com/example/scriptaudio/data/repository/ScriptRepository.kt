package com.example.scriptaudio.data.repository

import com.example.scriptaudio.data.local.ScriptEntity

/**
 * Repository 인터페이스
 */

interface ScriptRepository {

    suspend fun getAllScripts(): List<ScriptEntity>

    suspend fun insertScript(script: ScriptEntity)

}