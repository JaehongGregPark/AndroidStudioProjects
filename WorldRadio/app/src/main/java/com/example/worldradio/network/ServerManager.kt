package com.example.worldradio.network

/**
 * Radio Browser 서버 목록 관리
 *
 * 서버 장애 또는 지연 시 자동 전환
 */
object ServerManager {

    // 공식 미러 서버들
    private val servers = listOf(
        "https://nl1.api.radio-browser.info/json/",
        "https://de1.api.radio-browser.info/json/",
        "https://at1.api.radio-browser.info/json/",
        "https://fr1.api.radio-browser.info/json/"
    )

    /**
     * 서버 목록 반환 (랜덤 순서)
     */
    fun getShuffledServers(): List<String> {
        return servers.shuffled()
    }
}