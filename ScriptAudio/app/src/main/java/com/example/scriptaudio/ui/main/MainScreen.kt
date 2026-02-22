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
 */
@Composable
fun MainScreen(

    /**
     * 설정 화면 이동 콜백
     */
    onSettingsClick: () -> Unit,

    /**
     * Hilt ViewModel 가져오기
     *
     * 반드시 <MainViewModel> 타입 명시해야 함
     */
    viewModel: MainViewModel = hiltViewModel<MainViewModel>()

) {

    val script by viewModel.script.collectAsState()

    Column(
        modifier = Modifier.padding(16.dp)
    ) {

        TextField(

            value = script,

            onValueChange = {

                viewModel.updateScript(it)

            }

        )


        Spacer(modifier = Modifier.height(10.dp))


        Button(
            onClick = {
                viewModel.speak()
            }
        ) {

            Text("TTS 읽기")

        }


        Spacer(modifier = Modifier.height(10.dp))


        Button(
            onClick = onSettingsClick
        ) {

            Text("설정")

        }

    }

}