package com.example.audioscript.domain.model

/**
 * 문서 데이터를 표현하는 Domain Model
 *
 * @property fileName 파일 이름
 * @property content 문서 내용 전체 텍스트
 */
data class DocumentModel(
    val fileName: String,
    val content: String
)
