package com.example.pythonttsmvvmapp.data.parser

import android.net.Uri

/**
 * 문서를 텍스트로 변환하는 인터페이스
 * TXT / PDF 등 다양한 형식을 동일한 방식으로 처리하기 위함
 */
interface DocumentParser {
    suspend fun parse(uri: Uri): String
}
