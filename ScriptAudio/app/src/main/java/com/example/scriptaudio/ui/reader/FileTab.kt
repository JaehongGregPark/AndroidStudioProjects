package com.example.scriptaudio.ui.reader

// Android SAF 파일 선택
import android.net.Uri

// Compose Activity Result
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

// Compose Layout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

// LazyColumn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

// Material3
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text

// Compose Runtime
import androidx.compose.runtime.Composable

// Compose UI
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// ViewModel
import com.example.scriptaudio.viewmodel.MainViewModel

// File
import java.io.File

/**
 * FileTab
 *
 * 파일 관리 화면
 *
 * 기능
 * 1 파일 선택
 * 2 파일 목록
 * 3 파일 열기
 */
@Composable
fun FileTab(

    viewModel: MainViewModel,

    files: List<File>

) {

    val context = LocalContext.current

    /**
     * SAF 파일 선택기
     */
    val filePicker = rememberLauncherForActivityResult(

        ActivityResultContracts.OpenDocument()

    ) { uri: Uri? ->

        uri?.let {

            viewModel.openFileFromUri(

                context.contentResolver,

                it

            )

        }

    }

    Column(

        modifier = Modifier.fillMaxSize()

    ) {

        /**
         * 파일 선택 버튼
         */
        Button(

            onClick = {

                filePicker.launch(

                    arrayOf(

                        "text/plain",
                        "application/pdf"

                    )

                )

            },

            modifier = Modifier.padding(16.dp)

        ) {

            Text("파일 선택")

        }

        /**
         * 파일 목록
         */
        LazyColumn(

            modifier = Modifier.fillMaxSize()

        ) {

            items(files) { file ->

                ListItem(

                    headlineContent = {

                        Text(file.name)

                    },

                    modifier = Modifier

                        .clickable {

                            viewModel.openFile(file)

                        }

                )

            }

        }

    }

}