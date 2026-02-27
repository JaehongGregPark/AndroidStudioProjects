package com.example.scriptaudio.ui.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scriptaudio.viewmodel.MainViewModel

import java.io.File
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext

@Composable
fun MainScreen(

    onSettingsClick: () -> Unit,

    viewModel: MainViewModel = hiltViewModel()

) {

    val script by viewModel.script.collectAsState()
    val fileList by viewModel.fileList.collectAsState()



    /**
     * 시스템 파일 선택 런처
     */
    val context = LocalContext.current

    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->

            uri ?: return@rememberLauncherForActivityResult

            /**
             * 권한 유지 (앱 재시작 후에도 접근 가능)
             */
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            viewModel.openFileFromUri(
                context.contentResolver,
                uri
            )
        }



    /**
     * 최초 실행시 파일 로드
     */
    LaunchedEffect(Unit) {
        viewModel.loadFiles()
    }



    Column(
        modifier = Modifier.padding(16.dp)
    ) {


        TextField(
            value = script,
            onValueChange = {
                viewModel.updateScript(it)
            },
            modifier = Modifier.fillMaxWidth()
        )



        Spacer(modifier = Modifier.height(10.dp))



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
         * 🔥 파일 불러오기 버튼 추가
         */
        Button(
            onClick = {

                filePickerLauncher.launch(
                    arrayOf(
                        "text/plain",
                        "application/pdf"
                    )
                )

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("파일 불러오기")
        }



        Button(
            onClick = {
                viewModel.createSampleNovels()
                viewModel.loadFiles()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("신규소설 생성")
        }



        Spacer(modifier = Modifier.height(20.dp))



        Text("파일 목록")



        LazyColumn {

            items(fileList) { file ->

                FileItem(
                    file,
                    viewModel
                )

            }

        }

    }

}



@Composable
fun FileItem(

    file: File,

    viewModel: MainViewModel

) {

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .clickable {

                viewModel.openFile(file)

            }
            .padding(10.dp),

        horizontalArrangement =
            Arrangement.SpaceBetween

    ) {

        Text(
            file.name,
            modifier = Modifier.weight(1f)
        )



        Button(
            onClick = {
                viewModel.deleteFile(file)
            }
        ) {
            Text("삭제")
        }

    }

}