package com.example.ebookreader

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 앱 전체에서 사용하는 Application 클래스
 *
 * ✔ Hilt DI 컨테이너의 시작점
 * ✔ 앱 실행 시 가장 먼저 생성된다
 *
 * 반드시 AndroidManifest.xml 에
 * android:name=".EbookReaderApp" 등록 필요
 */
@HiltAndroidApp
class EbookReaderApp : Application()
