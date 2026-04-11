package com.example.scriptaudio

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt 시작점
 */
@HiltAndroidApp
class ScriptAudioApp : Application(){

    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(applicationContext)
    }
}