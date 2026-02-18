package com.example.audioscript.data.ml

import com.google.mlkit.nl.translate.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MLKit 기반 번역 엔진
 * - 자동 모델 다운로드
 * - suspend 지원
 */
@Singleton
class MLKitTranslator @Inject constructor() {

    suspend fun translate(
        text: String,
        sourceLang: String,
        targetLang: String
    ): String {

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()

        val translator = Translation.getClient(options)

        // 모델 다운로드 (없으면 자동 다운로드)
        translator.downloadModelIfNeeded().await()

        return translator.translate(text).await()
    }
}
