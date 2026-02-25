package com.example.scriptaudio.ui.main

import android.app.Activity
import android.content.Intent
import android.net.Uri

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.ui.platform.LocalContext

import androidx.hilt.navigation.compose.hiltViewModel

import com.example.scriptaudio.viewmodel.MainViewModel

import java.io.File


/**
 * 메인 화면
 */
@Composable
fun MainScreen(

    onSettingsClick: () -> Unit,

    viewModel: MainViewModel = hiltViewModel()

) {

    val script by viewModel.script.collectAsState()

    val context = LocalContext.current



    /**
     *
     * 파일 선택 런처
     *
     */
    val filePickerLauncher = rememberLauncherForActivityResult(

        contract =
            ActivityResultContracts.GetContent()

    ) { uri: Uri? ->

        uri ?: return@rememberLauncherForActivityResult


        /**
         * URI → File 변환
         */
        val file =
            File(uri.path ?: return@rememberLauncherForActivityResult)


        /**
         * ViewModel 호출
         */
        viewModel.openFile(file)

    }




    Column(

        modifier = Modifier.padding(16.dp)

    ) {


        /**
         *
         * 텍스트 표시
         *
         */
        TextField(

            value = script,

            onValueChange = {

                viewModel.updateScript(it)

            },

            modifier = Modifier.fillMaxWidth()

        )



        Spacer(modifier = Modifier.height(10.dp))



        /**
         * TTS 버튼
         */
        Button(

            onClick = {

                viewModel.speak()

            },

            modifier = Modifier.fillMaxWidth()

        ) {

            Text("TTS 읽기")

        }



        Spacer(modifier = Modifier.height(10.dp))



        /**
         * 파일 열기 버튼
         */
        Button(

            onClick = {

                filePickerLauncher.launch("*/*")

            },

            modifier = Modifier.fillMaxWidth()

        ) {

            Text("파일 열기")

        }



        Spacer(modifier = Modifier.height(10.dp))



        /**
         * 신규소설 생성
         */
        Button(

            onClick = {

                viewModel.createSampleNovels()

            },

            modifier = Modifier.fillMaxWidth()

        ) {

            Text("신규소설 생성")

        }



        Spacer(modifier = Modifier.height(10.dp))



        /**
         * 설정 버튼
         */
        Button(

            onClick = onSettingsClick,

            modifier = Modifier.fillMaxWidth()

        ) {

            Text("설정")

        }



    }

}