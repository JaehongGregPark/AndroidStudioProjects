package com.example.pythonttsmvvmapp.data.parser

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * PDF → 텍스트 변환
 *
 * 실제 사용 시 PDF 라이브러리 추가 필요
 */
class PdfParser @Inject constructor(
    @ApplicationContext private val context: Context
) : DocumentParser {

    override suspend fun parse(uri: Uri): String = withContext(Dispatchers.IO) {

        // TODO: PDFBox 같은 라이브러리 연동 위치
        // 현재는 동작 확인용 더미 반환

        "PDF 파싱 라이브러리를 연결하면 내용이 표시됩니다."
    }
}
