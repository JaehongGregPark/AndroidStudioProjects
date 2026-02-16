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
 * ???„ì²´?ì„œ ?¬ìš©?˜ëŠ” Application ?´ë˜??
 *
 * ??Hilt DI ì»¨í…Œ?´ë„ˆ???œì‘??
 * ?????¤í–‰ ??ê°€??ë¨¼ì? ?ì„±?œë‹¤
 *
 * ë°˜ë“œ??AndroidManifest.xml ??
 * android:name=".EbookReaderApp" ?±ë¡ ?„ìš”
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
 * Android API ë¥?ì§ì ‘ ?¬ìš©?˜ëŠ” ìµœí•˜??ê³„ì¸µ
 *
 * ??Uri ???Œì¼ëª?ì¶”ì¶œ
 * ???•ì¥??ê¸°ë°˜ Parser ? íƒ
 * ???ìŠ¤??ë³€??
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
 * SharedPreferences ë¥??´ìš©??
 * "?´ì–´ ?½ê¸° ?„ì¹˜" ?€???„ìš© DataSource
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
 * ë¬¸ì„œ ?Œì¼??ë¬¸ì?´ë¡œ ë³€?˜í•˜??ê³µí†µ ?¸í„°?˜ì´??
 *
 * TXT / PDF / EPUB ???•ì¥ ê°€??
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
 * PDF ?Œì¼ ?Œì„œ
 *
 * ???¤ì œ PDFBox ?°ë™ ?„ì¹˜
 */
class PdfParser @Inject constructor(
    @ApplicationContext private val context: Context
) : DocumentParser {

    override suspend fun parse(uri: Uri): String =
        withContext(Dispatchers.IO) {
            "PDF ?Œì‹± ?¼ì´ë¸ŒëŸ¬ë¦??°ë™ ?ˆì •"
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
 * TXT ?Œì¼ ?Œì„œ
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
 * ?Œì¼ ?Œì‹± ê²°ê³¼ë¥??œí˜„?˜ëŠ” ?„ë©”??ëª¨ë¸
 *
 * UI / ViewModel / UseCase ?ì„œ ?¬ìš©
 * Android API ?€ ë¬´ê????œìˆ˜ Kotlin ?°ì´???´ë˜??
 */
data class ParsedFile(
    val name: String,   // ?Œì¼ ?´ë¦„
    val text: String    // ?Œì¼ ?„ì²´ ?ìŠ¤??
)
package com.example.ebookreader.domain.repository

import android.content.Context
import android.net.Uri
import com.example.ebookreader.domain.model.ParsedFile

/**
 * Reader ê¸°ëŠ¥???°ì´??ì§„ì…??(Port)
 *
 * UseCase ?????¸í„°?˜ì´?¤ë§Œ ?˜ì¡´?œë‹¤.
 * ?¤ì œ êµ¬í˜„?€ data ê³„ì¸µ?ì„œ ?œê³µ?œë‹¤.
 */
interface ReaderRepository {

    /**
     * ?Œì¼???´ê³  ?ìŠ¤?¸ë¡œ ë³€??
     */
    suspend fun openFile(
        context: Context,
        uri: Uri
    ): ParsedFile

    /**
     * ë§ˆì?ë§??½ì? ?„ì¹˜ ?€??
     */
    fun saveReadingPosition(
        uri: String,
        start: Int,
        end: Int
    )

    /**
     * ë§ˆì?ë§??½ì? ?„ì¹˜ ë³µì›
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
 * Domain ??ReaderRepository êµ¬í˜„ì²?
 *
 * ?¬ëŸ¬ DataSource ë¥?ì¡°í•©?œë‹¤.
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
 * ?Œì¼ ?´ê¸° ? ìŠ¤ì¼€?´ìŠ¤
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
 * ?´ì–´ ?½ê¸° ?„ì¹˜ ?€??ë³µì› ? ìŠ¤ì¼€?´ìŠ¤
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
 * ?“Œ Reader ?”ë©´ ?íƒœ
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
 * ?“Œ ReaderViewModel (UI ?„ìš© ViewModel)
 *
 * ??• :
 * ??UI ?íƒœ ê´€ë¦?
 * ??UseCase ?¸ì¶œ
 *
 * ?˜ì? ?ŠëŠ” ê²?
 * ???Œì¼ ?Œì‹± ë¡œì§
 * ??Repository ì§ì ‘ ?‘ê·¼
 * ??Android Context ?¬ìš©
 */
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val loadBookUseCase: LoadBookUseCase
) : ViewModel() {

    /**
     * ?“Œ ?”ë©´?ì„œ ê´€ì°°í•˜??UI ?íƒœ
     */
    var uiState: ReaderUiState = ReaderUiState()
        private set

    /**
     * ?“– ?„ìì±?ë¡œë“œ
     *
     * @param bookPath ?¬ìš©?ê? ? íƒ???Œì¼ ê²½ë¡œ
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
     * ???¤ìŒ ?˜ì´ì§€
     */
    fun nextPage() {
        if (uiState.currentPage < uiState.pages.lastIndex) {
            uiState = uiState.copy(
                currentPage = uiState.currentPage + 1
            )
        }
    }

    /**
     * ?€ ?´ì „ ?˜ì´ì§€
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
 * ???„ì²´?ì„œ ?¬ìš©?˜ëŠ” Application ?´ë˜??
 *
 * ??Hilt DI ì»¨í…Œ?´ë„ˆ???œì‘??
 * ?????¤í–‰ ??ê°€??ë¨¼ì? ?ì„±?œë‹¤
 *
 * ë°˜ë“œ??AndroidManifest.xml ??
 * android:name=".EbookReaderApp" ?±ë¡ ?„ìš”
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
 * Android API ë¥?ì§ì ‘ ?¬ìš©?˜ëŠ” ìµœí•˜??ê³„ì¸µ
 *
 * ??Uri ???Œì¼ëª?ì¶”ì¶œ
 * ???•ì¥??ê¸°ë°˜ Parser ? íƒ
 * ???ìŠ¤??ë³€??
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
 * SharedPreferences ë¥??´ìš©??
 * "?´ì–´ ?½ê¸° ?„ì¹˜" ?€???„ìš© DataSource
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
 * ë¬¸ì„œ ?Œì¼??ë¬¸ì?´ë¡œ ë³€?˜í•˜??ê³µí†µ ?¸í„°?˜ì´??
 *
 * TXT / PDF / EPUB ???•ì¥ ê°€??
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
 * PDF ?Œì¼ ?Œì„œ
 *
 * ???¤ì œ PDFBox ?°ë™ ?„ì¹˜
 */
class PdfParser @Inject constructor(
    @ApplicationContext private val context: Context
) : DocumentParser {

    override suspend fun parse(uri: Uri): String =
        withContext(Dispatchers.IO) {
            "PDF ?Œì‹± ?¼ì´ë¸ŒëŸ¬ë¦??°ë™ ?ˆì •"
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
 * TXT ?Œì¼ ?Œì„œ
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
 * ?Œì¼ ?Œì‹± ê²°ê³¼ë¥??œí˜„?˜ëŠ” ?„ë©”??ëª¨ë¸
 *
 * UI / ViewModel / UseCase ?ì„œ ?¬ìš©
 * Android API ?€ ë¬´ê????œìˆ˜ Kotlin ?°ì´???´ë˜??
 */
data class ParsedFile(
    val name: String,   // ?Œì¼ ?´ë¦„
    val text: String    // ?Œì¼ ?„ì²´ ?ìŠ¤??
)
package com.example.ebookreader.domain.repository

import android.content.Context
import android.net.Uri
import com.example.ebookreader.domain.model.ParsedFile

/**
 * Reader ê¸°ëŠ¥???°ì´??ì§„ì…??(Port)
 *
 * UseCase ?????¸í„°?˜ì´?¤ë§Œ ?˜ì¡´?œë‹¤.
 * ?¤ì œ êµ¬í˜„?€ data ê³„ì¸µ?ì„œ ?œê³µ?œë‹¤.
 */
interface ReaderRepository {

    /**
     * ?Œì¼???´ê³  ?ìŠ¤?¸ë¡œ ë³€??
     */
    suspend fun openFile(
        context: Context,
        uri: Uri
    ): ParsedFile

    /**
     * ë§ˆì?ë§??½ì? ?„ì¹˜ ?€??
     */
    fun saveReadingPosition(
        uri: String,
        start: Int,
        end: Int
    )

    /**
     * ë§ˆì?ë§??½ì? ?„ì¹˜ ë³µì›
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
 * Domain ??ReaderRepository êµ¬í˜„ì²?
 *
 * ?¬ëŸ¬ DataSource ë¥?ì¡°í•©?œë‹¤.
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
 * ?Œì¼ ?´ê¸° ? ìŠ¤ì¼€?´ìŠ¤
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
 * ?´ì–´ ?½ê¸° ?„ì¹˜ ?€??ë³µì› ? ìŠ¤ì¼€?´ìŠ¤
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
 * ?“Œ Reader ?”ë©´ ?íƒœ
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
 * ?“Œ ReaderViewModel (UI ?„ìš© ViewModel)
 *
 * ??• :
 * ??UI ?íƒœ ê´€ë¦?
 * ??UseCase ?¸ì¶œ
 *
 * ?˜ì? ?ŠëŠ” ê²?
 * ???Œì¼ ?Œì‹± ë¡œì§
 * ??Repository ì§ì ‘ ?‘ê·¼
 * ??Android Context ?¬ìš©
 */
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val loadBookUseCase: LoadBookUseCase
) : ViewModel() {

    /**
     * ?“Œ ?”ë©´?ì„œ ê´€ì°°í•˜??UI ?íƒœ
     */
    var uiState: ReaderUiState = ReaderUiState()
        private set

    /**
     * ?“– ?„ìì±?ë¡œë“œ
     *
     * @param bookPath ?¬ìš©?ê? ? íƒ???Œì¼ ê²½ë¡œ
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
     * ???¤ìŒ ?˜ì´ì§€
     */
    fun nextPage() {
        if (uiState.currentPage < uiState.pages.lastIndex) {
            uiState = uiState.copy(
                currentPage = uiState.currentPage + 1
            )
        }
    }

    /**
     * ?€ ?´ì „ ?˜ì´ì§€
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
