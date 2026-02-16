package com.example.ebookreader.data.datasource

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * SharedPreferences 를 이용한
 * "이어 읽기 위치" 저장 전용 DataSource
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
