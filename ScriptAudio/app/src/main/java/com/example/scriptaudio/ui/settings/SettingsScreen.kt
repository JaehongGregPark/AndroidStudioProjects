package com.example.scriptaudio.ui.settings

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.hilt.navigation.compose.hiltViewModel

import com.example.scriptaudio.viewmodel.MainViewModel


@Composable
fun SettingsScreen(

    onBackClick: () -> Unit,

    viewModel: MainViewModel = hiltViewModel()

) {

    val rate by viewModel.speechRate.collectAsState()

    val pitch by viewModel.pitch.collectAsState()


    Column(

        modifier = Modifier.padding(16.dp)

    ) {

        Button(

            onClick = onBackClick

        ) {

            Text("뒤로가기")

        }


        Text("속도")


        Slider(

            value = rate,

            onValueChange = {

                viewModel.setSpeechRate(it)

            }

        )


        Text("Pitch")


        Slider(

            value = pitch,

            onValueChange = {

                viewModel.setPitch(it)

            }

        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                viewModel.createLargeSampleNovels()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔥 대용량 샘플 소설 생성 (20개)")
        }

    }

}