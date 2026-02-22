package com.example.scriptaudio.data.local

/**
 * Repository 인터페이스
 *
 * ViewModel → Repository → DAO
 *
 * 구조를 사용하면
 *
 * 유지보수 쉬워짐
 *
 */
interface ScriptRepository {

    suspend fun insert(script: ScriptEntity)

}