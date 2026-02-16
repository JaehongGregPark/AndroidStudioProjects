package com.example.ebookreader.data.parser

import android.net.Uri

/**
 * 문서 파일을 문자열로 변환하는 공통 인터페이스
 *
 * TXT / PDF / EPUB 등 확장 가능
 */
interface DocumentParser {
    suspend fun parse(uri: Uri): String
}
