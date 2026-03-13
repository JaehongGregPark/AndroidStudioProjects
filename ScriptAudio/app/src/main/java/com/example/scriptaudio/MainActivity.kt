package com.example.scriptaudio

// Android Activity 기본 클래스
import android.os.Bundle

// Compose Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

// Hilt Android Entry
import dagger.hilt.android.AndroidEntryPoint

// Navigation Graph
import com.example.scriptaudio.navigation.NavGraph

/**
 * MainActivity
 *
 * 앱의 시작 Activity
 *
 * 역할
 * - Compose UI 시작
 * - NavGraph 연결
 *
 * Android App → Activity → Compose → NavGraph
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * Compose UI 시작
         */
        setContent {

            /**
             * Navigation Graph 호출
             */
            NavGraph()

        }
    }
}