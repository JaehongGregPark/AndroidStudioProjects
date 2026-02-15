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
import androidx.compose.material3.Text

/**
 * 앱의 시작 지점
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Hilt가 ViewModel 자동 생성 */
    private val viewModel: ReaderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ⭐ 샘플 파일 자동 생성
        SampleFileInitializer.createSampleIfNeeded(this)
        /**
         * 앱이 종료되어도 음성이 유지되도록
         * Foreground Service 시작
         */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(
                Intent(this, TtsForegroundService::class.java)
            )
        }

        /**
         * Compose UI 시작
         */
        setContent {
            PythonTTSMVVMAppTheme {
                //val viewModel: ReaderViewModel = hiltViewModel()
                ReaderScreen(viewModel)
            }
            //Text("여기가 진짜 실행 화면이다")
        }
    }
}
