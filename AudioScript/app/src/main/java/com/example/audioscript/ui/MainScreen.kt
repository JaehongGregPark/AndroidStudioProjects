package com.example.audioscript.ui

/**
 * MainScreen.kt
 *
 * 메인 화면 (Compose UI)
 *
 * 역할:
 *
 * ✔ 텍스트 입력 및 편집
 * ✔ 번역 기능
 * ✔ 소설 생성
 * ✔ SettingsPanel 호출
 *
 * SettingsPanel 에서 처리하는 기능:
 * ✔ TXT 저장
 * ✔ PDF 저장
 * ✔ TTS 속도 조절
 * ✔ TTS 피치 조절
 * ✔ 음성 출력
 *
 * 아키텍처:
 *
 * MainScreen
 *  ├ Text Input
 *  ├ Translate
 *  ├ Story Generate
 *  └ SettingsPanel
 *
 * ViewModel:
 * MainViewModel 사용 (Hilt DI)
 */

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.hilt.navigation.compose.hiltViewModel

import com.example.audioscript.viewmodel.MainViewModel

/**
 * 메인 화면 Composable
 */
@Composable
fun MainScreen(

    /**
     * Hilt 를 통해 ViewModel 주입
     */
    viewModel: MainViewModel = hiltViewModel()

) {

    /**
     * ===============================
     * ViewModel State
     * ===============================
     */

    val text by viewModel.text.collectAsState()

    val speechRate by viewModel.speechRate.collectAsState()

    val pitch by viewModel.pitch.collectAsState()


    /**
     * ===============================
     * Local UI State
     * ===============================
     */

    var storyTitle by remember {

        mutableStateOf("")

    }

    var isKorean by remember {

        mutableStateOf(true)

    }


    /**
     * Scroll State
     */

    val scrollState = rememberScrollState()


    /**
     * ===============================
     * UI Layout
     * ===============================
     */

    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)

    ) {


        /**
         * ===============================
         * 텍스트 입력 영역
         * ===============================
         */

        OutlinedTextField(

            value = text,

            onValueChange = {

                viewModel.updateText(it)

            },

            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState),

            label = {

                Text("텍스트 입력")

            }

        )


        Spacer(modifier = Modifier.height(12.dp))



        /**
         * ===============================
         * 번역 버튼
         * ===============================
         */

        Button(

            onClick = {

                viewModel.translate()

            },

            modifier = Modifier.fillMaxWidth()

        ) {

            Text("🌍 번역")

        }


        Spacer(modifier = Modifier.height(12.dp))



        /**
         * ===============================
         * 소설 제목 입력
         * ===============================
         */

        OutlinedTextField(

            value = storyTitle,

            onValueChange = {

                storyTitle = it

            },

            label = {

                Text("소설 제목")

            },

            modifier = Modifier.fillMaxWidth()

        )



        /**
         * ===============================
         * 언어 선택 Switch
         * ===============================
         */

        Row(

            horizontalArrangement = Arrangement.SpaceBetween,

            modifier = Modifier.fillMaxWidth()

        ) {

            Text("한국어")

            Switch(

                checked = isKorean,

                onCheckedChange = {

                    isKorean = it

                }

            )

        }



        /**
         * ===============================
         * 소설 생성 버튼
         * ===============================
         */

        Button(

            onClick = {

                viewModel.generateStory(

                    storyTitle,
                    isKorean

                )

            },

            modifier = Modifier.fillMaxWidth()

        ) {

            Text("✍ 소설 생성")

        }



        Spacer(modifier = Modifier.height(16.dp))



        /**
         * ===============================
         * Settings Panel
         *
         * TXT 저장
         * PDF 저장
         * TTS 설정
         * 음성 출력
         * ===============================
         */

        SettingsPanel(

            speechRate = speechRate,

            pitch = pitch,


            onSpeechRateChange = {

                viewModel.setSpeechRate(it)

            },


            onPitchChange = {

                viewModel.setPitch(it)

            },


            onSpeak = {

                viewModel.speak()

            },


            onExportPdf = {

                viewModel.exportPdf("GeneratedStory")

            },


            onExportTxt = {

                viewModel.exportTxt("GeneratedStory")

            }

        )

    }

}
