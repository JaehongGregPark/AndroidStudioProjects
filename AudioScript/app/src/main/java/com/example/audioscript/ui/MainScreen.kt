package com.example.audioscript.ui

/**
 * MainScreen.kt
 *
 * 추가 기능:
 *
 * ✔ 파일읽기 버튼
 * ✔ 설정 버튼 (SettingsPanel 표시 / 숨김)
 *
 * 기존 기능:
 *
 * ✔ 텍스트 입력
 * ✔ 번역
 * ✔ 소설 생성
 * ✔ TXT 저장
 * ✔ PDF 저장
 * ✔ TTS
 */

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

import androidx.hilt.navigation.compose.hiltViewModel

import com.example.audioscript.viewmodel.MainViewModel

import java.io.BufferedReader
import java.io.InputStreamReader


@Composable
fun MainScreen(

    viewModel: MainViewModel = hiltViewModel()

) {

    val context = LocalContext.current


    /**
     * ViewModel 상태
     */

    val text by viewModel.text.collectAsState()

    val speechRate by viewModel.speechRate.collectAsState()

    val pitch by viewModel.pitch.collectAsState()



    /**
     * 로컬 상태
     */

    var storyTitle by remember { mutableStateOf("") }

    var isKorean by remember { mutableStateOf(true) }

    var showSettings by remember { mutableStateOf(false) }



    /**
     * 파일 선택 Launcher
     */

    val fileLauncher =
        rememberLauncherForActivityResult(

            contract = ActivityResultContracts.GetContent()

        ) { uri: Uri? ->

            uri?.let {

                val inputStream =
                    context.contentResolver.openInputStream(it)

                val reader =
                    BufferedReader(
                        InputStreamReader(inputStream)
                    )

                val fileText =
                    reader.readText()

                viewModel.updateText(fileText)

            }

        }



    val scrollState =
        rememberScrollState()



    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)

    ) {


        /**
         * =========================
         * 파일읽기 버튼
         * =========================
         */

        Button(

            onClick = {

                fileLauncher.launch("*/*")

            },

            modifier = Modifier.fillMaxWidth()

        ) {

            Text("📂 파일읽기")

        }



        Spacer(modifier = Modifier.height(8.dp))



        /**
         * =========================
         * 설정 버튼
         * =========================
         */

        Button(

            onClick = {

                showSettings = !showSettings

            },

            modifier = Modifier.fillMaxWidth()

        ) {

            Text("⚙ 설정")

        }



        Spacer(modifier = Modifier.height(12.dp))



        /**
         * 텍스트 입력
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
         * 번역
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
         * 소설 생성
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



        Row(

            modifier = Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween

        ) {

            Text("한국어")

            Switch(

                checked = isKorean,

                onCheckedChange = {

                    isKorean = it

                }

            )

        }



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
         * =========================
         * SettingsPanel
         * =========================
         */

        if (showSettings) {

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

}