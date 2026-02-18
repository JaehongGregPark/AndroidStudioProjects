package com.example.audioscript

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 애플리케이션 진입점 클래스
 *
 * - Hilt 의존성 주입을 사용하기 위해 반드시 필요
 * - AndroidManifest.xml에 name=".AudioScriptApp" 등록해야 함
 */
@HiltAndroidApp
class AudioScriptApp : Application() {
    override fun onCreate() {
        super.onCreate()
        com.tom_roush.pdfbox.android.PDFBoxResourceLoader.init(applicationContext)
    }
}