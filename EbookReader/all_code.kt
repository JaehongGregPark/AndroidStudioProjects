package com.example.ebookreader

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.ebookreader", appContext.packageName)
    }
}
package com.example.ebookreader

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * ???꾩껜?먯꽌 ?ъ슜?섎뒗 Application ?대옒??
 *
 * ??Hilt DI 而⑦뀒?대꼫???쒖옉??
 * ?????ㅽ뻾 ??媛??癒쇱? ?앹꽦?쒕떎
 *
 * 諛섎뱶??AndroidManifest.xml ??
 * android:name=".EbookReaderApp" ?깅줉 ?꾩슂
 */
@HiltAndroidApp
class EbookReaderApp : Application()
package com.example.ebookreader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ebookreader.ui.theme.EbookReaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EbookReaderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EbookReaderTheme {
        Greeting("Android")
    }
}
package com.example.ebookreader.data.datasource

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.ebookreader.data.parser.PdfParser
import com.example.ebookreader.data.parser.TxtParser
import com.example.ebookreader.domain.model.ParsedFile
import javax.inject.Inject

/**
 * Android API 瑜?吏곸젒 ?ъ슜?섎뒗 理쒗븯??怨꾩링
 *
 * ??Uri ???뚯씪紐?異붿텧
 * ???뺤옣??湲곕컲 Parser ?좏깮
 * ???띿뒪??蹂??
 */
class FileDataSource @Inject constructor(
    private val txtParser: TxtParser,
    private val pdfParser: PdfParser
) {

    suspend fun parse(
        context: Context,
        uri: Uri
    ): ParsedFile {

        val name = extractFileName(context, uri)

        val text = when {
            name.endsWith(".txt", true) -> txtParser.parse(uri)
            name.endsWith(".pdf", true) -> pdfParser.parse(uri)
            else -> ""
        }

        return ParsedFile(name, text)
    }

    private fun extractFileName(
        context: Context,
        uri: Uri
    ): String {
        var name = "unknown"
        context.contentResolver.query(uri, null, null, null, null)?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && index >= 0) {
                name = it.getString(index)
            }
        }
        return name
    }
}
package com.example.ebookreader.data.datasource

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * SharedPreferences 瑜??댁슜??
 * "?댁뼱 ?쎄린 ?꾩튂" ????꾩슜 DataSource
 */
class PreferenceDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun save(uri: String, start: Int, end: Int) {
        context.getSharedPreferences("reading_position", Context.MODE_PRIVATE)
            .edit()
            .putInt("${uri}_start", start)
            .putInt("${uri}_end", end)
            .apply()
    }

    fun restore(uri: String): Pair<Int, Int> {
        val pref =
            context.getSharedPreferences("reading_position", Context.MODE_PRIVATE)
        return pref.getInt("${uri}_start", -1) to
                pref.getInt("${uri}_end", -1)
    }
}
package com.example.ebookreader.data.local.dao

class ReadingDao {
}
package com.example.ebookreader.data.local.db

class AppDatabase {
}
package com.example.ebookreader.data.local.entity

class ReadingFile {
}
package com.example.ebookreader.data.parser

import android.net.Uri

/**
 * 臾몄꽌 ?뚯씪??臾몄옄?대줈 蹂?섑븯??怨듯넻 ?명꽣?섏씠??
 *
 * TXT / PDF / EPUB ???뺤옣 媛??
 */
interface DocumentParser {
    suspend fun parse(uri: Uri): String
}
package com.example.ebookreader.data.parser

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * PDF ?뚯씪 ?뚯꽌
 *
 * ???ㅼ젣 PDFBox ?곕룞 ?꾩튂
 */
class PdfParser @Inject constructor(
    @ApplicationContext private val context: Context
) : DocumentParser {

    override suspend fun parse(uri: Uri): String =
        withContext(Dispatchers.IO) {
            "PDF ?뚯떛 ?쇱씠釉뚮윭由??곕룞 ?덉젙"
        }
}
package com.example.ebookreader.data.parser

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * TXT ?뚯씪 ?뚯꽌
 */
class TxtParser @Inject constructor(
    @ApplicationContext private val context: Context
) : DocumentParser {

    override suspend fun parse(uri: Uri): String =
        withContext(Dispatchers.IO) {
            context.contentResolver
                .openInputStream(uri)
                ?.bufferedReader()
                ?.use { it.readText() } ?: ""
        }
}
package com.example.ebookreader.di

class DatabaseModule {
}
package com.example.ebookreader.di

class ParserModule {
}
package com.example.ebookreader.di

class RepositoryModule {
}
package com.example.ebookreader.domain.model

/**
 * ?뚯씪 ?뚯떛 寃곌낵瑜??쒗쁽?섎뒗 ?꾨찓??紐⑤뜽
 *
 * UI / ViewModel / UseCase ?먯꽌 ?ъ슜
 * Android API ? 臾닿????쒖닔 Kotlin ?곗씠???대옒??
 */
data class ParsedFile(
    val name: String,   // ?뚯씪 ?대쫫
    val text: String    // ?뚯씪 ?꾩껜 ?띿뒪??
)
package com.example.ebookreader.domain.repository

import android.content.Context
import android.net.Uri
import com.example.ebookreader.domain.model.ParsedFile

/**
 * Reader 湲곕뒫???곗씠??吏꾩엯??(Port)
 *
 * UseCase ?????명꽣?섏씠?ㅻ쭔 ?섏〈?쒕떎.
 * ?ㅼ젣 援ы쁽? data 怨꾩링?먯꽌 ?쒓났?쒕떎.
 */
interface ReaderRepository {

    /**
     * ?뚯씪???닿퀬 ?띿뒪?몃줈 蹂??
     */
    suspend fun openFile(
        context: Context,
        uri: Uri
    ): ParsedFile

    /**
     * 留덉?留??쎌? ?꾩튂 ???
     */
    fun saveReadingPosition(
        uri: String,
        start: Int,
        end: Int
    )

    /**
     * 留덉?留??쎌? ?꾩튂 蹂듭썝
     */
    fun restoreReadingPosition(
        uri: String
    ): Pair<Int, Int>
}
package com.example.ebookreader.data.repository

import android.content.Context
import android.net.Uri
import com.example.ebookreader.data.datasource.FileDataSource
import com.example.ebookreader.data.datasource.PreferenceDataSource
import com.example.ebookreader.domain.repository.ReaderRepository
import javax.inject.Inject

/**
 * Domain ??ReaderRepository 援ы쁽泥?
 *
 * ?щ윭 DataSource 瑜?議고빀?쒕떎.
 */
class ReaderRepositoryImpl @Inject constructor(
    private val fileDataSource: FileDataSource,
    private val preferenceDataSource: PreferenceDataSource
) : ReaderRepository {

    override suspend fun openFile(
        context: Context,
        uri: Uri
    ) = fileDataSource.parse(context, uri)

    override fun saveReadingPosition(
        uri: String,
        start: Int,
        end: Int
    ) = preferenceDataSource.save(uri, start, end)

    override fun restoreReadingPosition(uri: String) =
        preferenceDataSource.restore(uri)
}
package com.example.ebookreader.domain.usecase

import android.content.Context
import android.net.Uri
import com.example.ebookreader.domain.repository.ReaderRepository
import javax.inject.Inject

/**
 * ?뚯씪 ?닿린 ?좎뒪耳?댁뒪
 *
 * UI ??ViewModel ??UseCase ??Repository
 */
class OpenFileUseCase @Inject constructor(
    private val repository: ReaderRepository
) {

    suspend operator fun invoke(
        context: Context,
        uri: Uri
    ) = repository.openFile(context, uri)
}
package com.example.ebookreader.domain.usecase

import com.example.ebookreader.domain.repository.ReaderRepository
import javax.inject.Inject

/**
 * ?댁뼱 ?쎄린 ?꾩튂 ???蹂듭썝 ?좎뒪耳?댁뒪
 */
class ReadingPositionUseCase @Inject constructor(
    private val repository: ReaderRepository
) {

    fun save(uri: String, start: Int, end: Int) =
        repository.saveReadingPosition(uri, start, end)

    fun restore(uri: String) =
        repository.restoreReadingPosition(uri)
}
package com.example.ebookreader.domain.usecase

class SpeakUseCase {
}
package com.example.ebookreader.reader.model

/**
 * ?뱦 Reader ?붾㈃ ?곹깭
 */
data class ReaderUiState(
    val isLoading: Boolean = false,
    val pages: List<String> = emptyList(),
    val currentPage: Int = 0,
    val errorMessage: String? = null
)
package com.example.ebookreader.reader.ui

class ReaderScreen {
}
package com.example.ebookreader.reader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ebookreader.reader.model.ReaderUiState
import com.example.ebookreader.reader.usecase.LoadBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ?뱦 ReaderViewModel (UI ?꾩슜 ViewModel)
 *
 * ??븷:
 * ??UI ?곹깭 愿由?
 * ??UseCase ?몄텧
 *
 * ?섏? ?딅뒗 寃?
 * ???뚯씪 ?뚯떛 濡쒖쭅
 * ??Repository 吏곸젒 ?묎렐
 * ??Android Context ?ъ슜
 */
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val loadBookUseCase: LoadBookUseCase
) : ViewModel() {

    /**
     * ?뱦 ?붾㈃?먯꽌 愿李고븯??UI ?곹깭
     */
    var uiState: ReaderUiState = ReaderUiState()
        private set

    /**
     * ?뱰 ?꾩옄梨?濡쒕뱶
     *
     * @param bookPath ?ъ슜?먭? ?좏깮???뚯씪 寃쎈줈
     */
    fun loadBook(bookPath: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            val result = loadBookUseCase(bookPath)

            uiState = if (result.isSuccess) {
                uiState.copy(
                    isLoading = false,
                    pages = result.getOrDefault(emptyList()),
                    currentPage = 0,
                    errorMessage = null
                )
            } else {
                uiState.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
        }
    }

    /**
     * ???ㅼ쓬 ?섏씠吏
     */
    fun nextPage() {
        if (uiState.currentPage < uiState.pages.lastIndex) {
            uiState = uiState.copy(
                currentPage = uiState.currentPage + 1
            )
        }
    }

    /**
     * ? ?댁쟾 ?섏씠吏
     */
    fun previousPage() {
        if (uiState.currentPage > 0) {
            uiState = uiState.copy(
                currentPage = uiState.currentPage - 1
            )
        }
    }
}
package com.example.ebookreader.service

class TtsForegroundService {
}
package com.example.ebookreader.tts

class TtsManager {
}
package com.example.ebookreader.tts

class TtsState {
}
package com.example.ebookreader.ui

class FilePicker {
}
package com.example.ebookreader.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
package com.example.ebookreader.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun EbookReaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
package com.example.ebookreader.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)
package com.example.ebookreader.util

class SampleFileInitializer {
}
package com.example.ebookreader

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
