package com.example.scriptaudio.ui.reader

// Compose 기본 레이아웃
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

// Material3 UI 컴포넌트
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar

// Compose State 관리
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

// Compose Modifier
import androidx.compose.ui.Modifier

// Material Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings

// ViewModel
import com.example.scriptaudio.viewmodel.MainViewModel

/**
 * ReaderScreen
 *
 * ScriptAudio 메인 UI
 *
 * 기능
 * 1 텍스트 보기
 * 2 번역 보기
 * 3 파일 목록
 * 4 TTS
 *
 * 탭 구조
 *
 * 원문 | 번역 | 파일
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(

    viewModel: MainViewModel,

    onOpenSettings: () -> Unit

) {

    /**
     * 현재 선택된 탭
     */
    val selectedTab = remember { mutableStateOf(0) }

    /**
     * ViewModel 상태 구독
     */
    val originalText by viewModel.originalText.collectAsState()

    val translatedText by viewModel.translatedText.collectAsState()

    val files by viewModel.fileList.collectAsState()

    Scaffold(

        /**
         * 상단 AppBar
         */
        topBar = {

            TopAppBar(

                title = {

                    Text("ScriptAudio")

                },

                actions = {

                    IconButton(

                        onClick = onOpenSettings

                    ) {

                        Icon(

                            imageVector = Icons.Default.Settings,

                            contentDescription = "Settings"

                        )

                    }

                }

            )

        }

    ) { padding ->

        Column(

            modifier = Modifier
                .padding(padding)
                .fillMaxSize()

        ) {

            /**
             * 상단 탭 UI
             */
            TabRow(

                selectedTabIndex = selectedTab.value

            ) {

                Tab(

                    selected = selectedTab.value == 0,

                    onClick = {

                        selectedTab.value = 0

                    },

                    text = {

                        Text("원문")

                    }

                )

                Tab(

                    selected = selectedTab.value == 1,

                    onClick = {

                        selectedTab.value = 1

                    },

                    text = {

                        Text("번역")

                    }

                )

                Tab(

                    selected = selectedTab.value == 2,

                    onClick = {

                        selectedTab.value = 2

                    },

                    text = {

                        Text("파일")

                    }

                )

            }

            /**
             * 탭 화면 전환
             */
            when (selectedTab.value) {

                /**
                 * 원문 탭
                 */
                0 -> OriginalTab(

                    text = originalText

                )

                /**
                 * 번역 탭
                 */
                1 -> TranslationTab(

                    text = translatedText,

                    onTranslate = {

                        viewModel.translate()

                    }

                )

                /**
                 * 파일 탭
                 */
                2 -> FileTab(

                    viewModel = viewModel,

                    files = files

                )

            }

        }

    }

}