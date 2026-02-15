package com.example.pythonttsmvvmapp.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * 시스템 파일 선택기
 */
@Composable
fun FilePicker(onFileSelected: (Uri) -> Unit) {

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { onFileSelected(it) }
    }

    Button(
        onClick = {
            launcher.launch(arrayOf("text/plain", "application/pdf"))
        }
    ) {
        Text("파일 열기")
    }
}
