package com.example.scriptaudio.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

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
}