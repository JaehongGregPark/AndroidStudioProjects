package com.example.pythonttsmvvmapp.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.pythonttsmvvmapp.service.TtsForegroundService
import com.example.pythonttsmvvmapp.ui.theme.PythonTTSMVVMAppTheme
import com.example.pythonttsmvvmapp.viewmodel.ReaderViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.pythonttsmvvmapp.util.SampleFileInitializer

/**
 * 🚀 앱의 진입점 (가장 먼저 실행되는 Activity)
 *
 * 여기서 하는 일:
 * ✔ ViewModel 준비
 * ✔ 샘플 파일 준비
 * ✔ Foreground Service 시작
 * ✔ Compose UI 시작
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * Hilt 를 이용해 ViewModel 자동 생성
     *
     * Activity 와 생명주기를 같이 하며,
     * 화면 회전이 되어도 유지된다.
     */
    private val viewModel: ReaderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * ⭐ 앱 최초 실행 시 샘플 TXT / PDF 파일 생성
         *
         * 사용자가 바로 테스트 가능하도록 준비한다.
         */
        SampleFileInitializer.createSampleIfNeeded(this)

        /**
         * ⭐ 음성이 앱 밖에서도 계속 재생되도록
         * Foreground Service 실행
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(
                Intent(this, TtsForegroundService::class.java)
            )
        }

        /**
         * ⭐ Compose UI 시작 지점
         */
        setContent {
            PythonTTSMVVMAppTheme {

                /**
                 * 현재는 ReaderScreen 하나만 사용하지만
                 *
                 * 나중에
                 * - 최근 파일 화면
                 * - 설정 화면
                 * - 음성 선택 화면
                 *
                 * 등을 추가하면 여기서 Navigation 을 붙이게 된다.
                 */
                ReaderScreen(
                    context = this,
                    viewModel = viewModel,
                    openRecent = {
                        // TODO: 최근 파일 화면으로 이동 기능 추가 예정
                    }
                )
            }
        }
    }
}
