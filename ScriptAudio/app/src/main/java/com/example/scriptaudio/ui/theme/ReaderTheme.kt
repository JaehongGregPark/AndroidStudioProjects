package com.example.scriptaudio.ui.theme

/**
 * ReaderTheme
 *
 * ScriptAudio 앱 테마
 */

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(

    primary = androidx.compose.ui.graphics.Color(0xFF3F51B5),

    secondary = androidx.compose.ui.graphics.Color(0xFF03DAC5)

)

@Composable
fun ReaderTheme(

    content: @Composable () -> Unit

) {

    MaterialTheme(

        colorScheme = LightColors,

        typography = Typography(),

        content = content

    )

}