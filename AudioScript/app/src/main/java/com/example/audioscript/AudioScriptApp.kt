package com.example.audioscript

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class AudioScriptApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // ✅ PDFBox 초기화 (반드시 필요)
        PDFBoxResourceLoader.init(applicationContext)
    }
}