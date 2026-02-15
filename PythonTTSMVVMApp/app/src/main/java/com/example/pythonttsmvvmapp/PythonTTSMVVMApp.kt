package com.example.pythonttsmvvmapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 앱 시작 시 Hilt DI 컨테이너 생성
 * 반드시 Manifest에 등록해야 함
 */
@HiltAndroidApp
class PythonTTSMVVMApp : Application()
