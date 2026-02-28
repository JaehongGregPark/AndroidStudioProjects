package com.example.scriptaudio.ui.main

import android.content.Intent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext

import androidx.hilt.navigation.compose.hiltViewModel

import com.example.scriptaudio.viewmodel.MainViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(

    onSettingsClick: () -> Unit,

    viewModel: MainViewModel = hiltViewModel()

) {

    val script by viewModel.script.collectAsState()
    val fileList by viewModel.fileList.collectAsState()

    val context = LocalContext.current


    /**
     * SAF 파일 선택기 (완전 안전)
     */
    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri: Uri? ->

            uri ?: return@rememberLauncherForActivityResult

            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            viewModel.openFileFromUri(
                context.contentResolver,
                uri
            )
        }



    LaunchedEffect(Unit) {
        viewModel.loadFiles()
        viewModel.preloadTranslationModel()
    }



    Scaffold(

        topBar = {

            TopAppBar(

                title = { Text("ScriptAudio") },

                actions = {

                    /**
                     * 🔥 설정 버튼 복구
                     */
                    TextButton(
                        onClick = onSettingsClick
                    ) {
                        Text("설정")
                    }

                }

            )

        }

    ) { paddingValues ->



        Column(

            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)

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
                    viewModel.translate()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("번역")
            }


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



            Spacer(modifier = Modifier.height(10.dp))



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

        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically

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