package com.example.app.domain.model

/**
 * 앱의 핵심 비즈니스 모델
 * 어떤 데이터 소스에도 의존하지 않음
 */
data class User(
    val id: Long,
    val name: String,
    val email: String
)

