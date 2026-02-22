package com.example.scriptaudio.ui.main

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scriptaudio.viewmodel.MainViewModel

/**
 * 메인 화면
 *
 * 기능:
 * ✔ TTS 읽기
 * ✔ TXT 저장
 * ✔ PDF 저장
 * ✔ TXT 불러오기
 * ✔ 설정 이동
 */
@Composable
fun MainScreen(

    // ✔ NavGraph 에서 전달하는 파라미터 이름과 반드시 동일해야 함
    onSettingsClick: () -> Unit,

    viewModel: MainViewModel = hiltViewModel()

) {

    val script by viewModel.script.collectAsState()

    Column(

        modifier = Modifier.padding(16.dp)

    ) {

        /**
         * 스크립트 입력창
         */
        OutlinedTextField(

            value = script,

            onValueChange = {

                viewModel.updateScript(it)

            },

            modifier = Modifier.fillMaxWidth(),

            label = {

                Text("스크립트")

            }

        )

        Spacer(modifier = Modifier.height(16.dp))

        /**
         * TTS 읽기 버튼
         */
        Button(

            onClick = {

                viewModel.speak()

            }

        ) {

            Text("읽기")

        }

        Spacer(modifier = Modifier.height(8.dp))

        /**
         * 설정 이동 버튼
         */
        Button(

            onClick = onSettingsClick

        ) {

            Text("설정")

        }

    }

}