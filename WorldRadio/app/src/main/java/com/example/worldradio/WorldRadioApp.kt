package com.example.worldradio

import android.app.Application   // 🔥 반드시 있어야 함
import dagger.hilt.android.HiltAndroidApp

/**
 * Application 클래스
 *
 * Hilt(의존성 주입)의 시작 지점.
 * 앱이 실행될 때 가장 먼저 생성된다.
 *
 * 반드시 AndroidManifest.xml 에 등록해야 한다.
 */
@HiltAndroidApp
class WorldRadioApp : Application()