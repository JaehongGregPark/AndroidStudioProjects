package com.example.stockquoteapp.data

/**
 * WebSocketManager
 *
 * 역할:
 * - 서버와 WebSocket 연결
 * - 실시간 데이터 수신
 * - 콜백으로 ViewModel 전달
 */

import okhttp3.*
import okio.ByteString

class WebSocketManager {

    private val client = OkHttpClient()

    private var webSocket: WebSocket? = null

    /**
     * 데이터 수신 콜백
     */
    var onMessageReceived: ((String) -> Unit)? = null

    /**
     * 연결 시작
     */
    fun connect() {

        val request = Request.Builder()
            // 예시 (실제는 API 제공 서버 사용 필요)
            .url("wss://example.com/realtime")
            .get()
            .header("User-Agent", "Mozilla/5.0")
            .build()

        webSocket = client.newWebSocket(
            request,
            object : WebSocketListener() {

                override fun onOpen(ws: WebSocket, response: Response) {
                    // 구독 요청 (종목 리스트)
                    ws.send("""{"type":"subscribe","symbols":["AAPL","TSLA"]}""")
                }

                override fun onMessage(ws: WebSocket, text: String) {
                    onMessageReceived?.invoke(text)
                }

                override fun onMessage(ws: WebSocket, bytes: ByteString) {
                    onMessageReceived?.invoke(bytes.utf8())
                }

                override fun onFailure(
                    ws: WebSocket,
                    t: Throwable,
                    response: Response?
                ) {
                    reconnect()
                }

                override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                    reconnect()
                }
            }
        )
    }

    /**
     * 재연결
     */
    private fun reconnect() {
        connect()
    }

    /**
     * 연결 종료
     */
    fun close() {
        webSocket?.close(1000, "close")
    }
}