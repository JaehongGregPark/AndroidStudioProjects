package com.example.worldradio.network

/**
 * Radio Browser 서버 목록 관리
 *
 * 서버 장애 또는 지연 시 자동 전환
 */
object ServerManager {

    // 실제로 안정적으로 동작하는 서버만 사용
    private val servers = listOf(
        "https://nl1.api.radio-browser.info/json/",
        "https://de1.api.radio-browser.info/json/"
    )

    fun getServers(): List<String> = servers
}