package com.example.pythonttsmvvmapp.util

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * 앱 최초 실행 시
 * 테스트용 샘플 txt 파일을 자동 생성한다.
 */
object SampleFileInitializer {

    fun createSampleIfNeeded(context: Context) {

        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        val file = File(downloadDir, "sample_tts.txt")

        // 이미 있으면 아무것도 안 함
        if (file.exists()) return

        file.writeText(
            """
            안녕하세요.
            이 파일은 자동으로 생성된 TTS 샘플입니다.
            앱이 정상적으로 동작하는지 확인할 수 있습니다.
            감사합니다.
            """.trimIndent()
        )
    }
}
