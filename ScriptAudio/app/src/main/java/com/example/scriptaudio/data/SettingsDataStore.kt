package com.example.scriptaudio.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

val FOLLOW_SYSTEM = booleanPreferencesKey("follow_system")
val AMOLED_BLACK = booleanPreferencesKey("amoled_black")
val THEME_COLOR = stringPreferencesKey("theme_color")
val FONT_FAMILY = stringPreferencesKey("font_family")

class SettingsDataStore(private val context: Context) {

    companion object {
        val TTS_SPEED = floatPreferencesKey("tts_speed")
        val FONT_SIZE = floatPreferencesKey("font_size")
        val SCROLL_SPEED = floatPreferencesKey("scroll_speed")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val ttsSpeed: Flow<Float> = context.dataStore.data.map {
        it[TTS_SPEED] ?: 1.0f
    }

    val fontSize: Flow<Float> = context.dataStore.data.map {
        it[FONT_SIZE] ?: 18f
    }

    val scrollSpeed: Flow<Float> = context.dataStore.data.map {
        it[SCROLL_SPEED] ?: 1f
    }

    val darkMode: Flow<Boolean> = context.dataStore.data.map {
        it[DARK_MODE] ?: false
    }

    val followSystem: Flow<Boolean> = context.dataStore.data.map {
        it[FOLLOW_SYSTEM] ?: true
    }

    val amoledBlack: Flow<Boolean> = context.dataStore.data.map {
        it[AMOLED_BLACK] ?: false
    }

    val themeColor: Flow<String> = context.dataStore.data.map {
        it[THEME_COLOR] ?: "blue"
    }

    val fontFamily: Flow<String> = context.dataStore.data.map {
        it[FONT_FAMILY] ?: "default"
    }

    suspend fun setTtsSpeed(v: Float) {
        context.dataStore.edit { it[TTS_SPEED] = v }
    }

    suspend fun setFontSize(v: Float) {
        context.dataStore.edit { it[FONT_SIZE] = v }
    }

    suspend fun setScrollSpeed(v: Float) {
        context.dataStore.edit { it[SCROLL_SPEED] = v }
    }

    suspend fun setDarkMode(v: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = v }
    }

    suspend fun setFollowSystem(v: Boolean) {
        context.dataStore.edit {
            it[FOLLOW_SYSTEM] = v
        }
    }

    suspend fun setAmoledBlack(v: Boolean) {
        context.dataStore.edit {
            it[AMOLED_BLACK] = v
        }
    }

    suspend fun setThemeColor(v: String) {
        context.dataStore.edit {
            it[THEME_COLOR] = v
        }
    }

    suspend fun setFontFamily(v: String) {
        context.dataStore.edit {
            it[FONT_FAMILY] = v
        }
    }
}