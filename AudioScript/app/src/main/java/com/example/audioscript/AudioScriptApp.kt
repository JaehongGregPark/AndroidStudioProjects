package com.example.audioscript

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp

/**
 * 앱 진입점
 * - Hilt 사용을 위한 필수 클래스
 * - PDFBox 초기화
 */
@HiltAndroidApp
class AudioScriptApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // PDFBox 리소스 초기화
        PDFBoxResourceLoader.init(applicationContext)
    }
}
