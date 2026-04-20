package com.example.stockquoteapp.data

/**
 * StockQuoteRepository (안정 버전)
 *
 * 특징:
 * - REST API 전용 (WebSocket 완전 분리)
 * - OkHttp 안전 사용
 * - GET만 사용 (POST 제거)
 * - response.body 1회만 읽기
 * - IO 스레드에서만 호출
 */

