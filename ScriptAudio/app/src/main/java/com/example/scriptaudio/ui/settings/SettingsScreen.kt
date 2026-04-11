package com.example.scriptaudio.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.scriptaudio.viewmodel.MainViewModel
import androidx.navigation.NavHostController

/**
 * SettingsScreen
 *
 * Reader 설정 화면
 *
 * 설정 항목
 * - TTS 속도
 * - 글자 크기
 * - 자동 스크롤 속도
 *
 * 특징
 * - ViewModel 상태 공유
 * - NavGraph 뒤로가기 지원
 * - 시스템 back 버튼 지원
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    // NavGraph에서 전달되는 뒤로가기 콜백
    //onBack: () -> Unit = {}
    navController: NavHostController,
    viewModel: MainViewModel
) {

    /**
     * 시스템 뒤로가기 버튼 처리
     *
     * - 안드로이드 물리 back
     * - 제스처 back
     */
    BackHandler {
        //onBack()
        navController.popBackStack()
    }

    /**
     * ViewModel 에서 TTS 속도 상태 구독
     * 설정 변경 시 ReaderScreen 자동 반영
     */
    val ttsSpeed by viewModel.ttsSpeed.collectAsState()

    /**
     * 글자 크기 상태
     * (추후 ViewModel 이동 가능)
     */
    var fontSize by remember { mutableStateOf(18f) }

    /**
     * 자동 스크롤 속도 상태
     */
    var scrollSpeed by remember { mutableStateOf(1f) }

    val darkMode by viewModel.darkMode.collectAsState()

    val follow by viewModel.followSystem.collectAsState()
    val amoled by viewModel.amoledBlack.collectAsState()
    val theme by viewModel.themeColor.collectAsState()
    val font by viewModel.fontFamily.collectAsState()

    /**
     * 전체 화면 레이아웃
     */
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reader Settings") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Text("←")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Text("TTS Speed: ${String.format("%.1f", ttsSpeed)}x")
                Slider(
                    value = ttsSpeed,
                    onValueChange = { viewModel.setTtsSpeed(it) },
                    valueRange = 0.5f..2.0f
                )
            }

            item {
                Text("Font Size: ${fontSize.toInt()}")
                Slider(
                    value = fontSize,
                    onValueChange = { fontSize = it },
                    valueRange = 12f..40f
                )
            }

            item {
                Text("Auto Scroll Speed: ${String.format("%.1f", scrollSpeed)}x")
                Slider(
                    value = scrollSpeed,
                    onValueChange = { scrollSpeed = it },
                    valueRange = 0.5f..5f
                )
            }

            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Dark Mode")

                    Switch(
                        checked = darkMode,
                        onCheckedChange = {
                            viewModel.setDarkMode(it)
                        }
                    )
                }
            }

            item {

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Follow System Theme")

                    Switch(
                        checked = follow,
                        onCheckedChange = {
                            viewModel.setFollowSystem(it)
                        }
                    )
                }
            }

            item {

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("AMOLED Black")

                    Switch(
                        checked = amoled,
                        onCheckedChange = {
                            viewModel.setAmoledBlack(it)
                        }
                    )
                }
            }

            item {

                Text("Theme Color")

                Row {

                    listOf("blue","green","purple","orange").forEach {

                        Button(
                            onClick = { viewModel.setThemeColor(it) }
                        ) {
                            Text(it)
                        }
                    }
                }
            }

            item {

                Text("Font")

                Row {

                    listOf("default","serif","mono","sans").forEach {

                        Button(
                            onClick = { viewModel.setFontFamily(it) }
                        ) {
                            Text(it)
                        }
                    }
                }
            }

        }
    }
}