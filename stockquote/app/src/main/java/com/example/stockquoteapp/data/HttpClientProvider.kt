package com.example.stockquoteapp.data

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * OkHttp 공용 클라이언트
 *
 * 목적:
 * - WebSocket + REST 동일 client 사용
 * - Dispatcher 충돌 방지
 * - connection pool 공유
 */
object HttpClientProvider {

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()

            // 연결 타임아웃
            .connectTimeout(10, TimeUnit.SECONDS)

            // 읽기 타임아웃
            .readTimeout(15, TimeUnit.SECONDS)

            // 재시도
            .retryOnConnectionFailure(true)

            .build()
    }
}