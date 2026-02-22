package com.example.scriptaudio.data.local

import javax.inject.Inject

/**
 * Repository 구현체
 *
 * 역할:
 * 인터페이스 ScriptRepository 실제 동작 구현
 *
 * Hilt가 자동으로 생성할 수 있도록
 * @Inject constructor 사용
 */
class ScriptRepositoryImpl @Inject constructor(

    /**
     * Room DAO 주입
     */
    private val dao: ScriptDao

) : ScriptRepository {

    /**
     * 스크립트 저장
     */
    override suspend fun insert(script: ScriptEntity) {

        dao.insert(script)

    }

}