package com.example.pythonttsmvvmapp.reader.data.datasource

import android.content.Context
import javax.inject.Inject

/**
 * 로컬 저장 담당
 * 최근 파일 / 이어 읽기
 */
class PreferenceDataSource @Inject constructor() {

    fun saveLastPosition(
        context: Context,
        uri: String,
        start: Int,
        end: Int
    ) {
        val pref = context.getSharedPreferences("last_read", Context.MODE_PRIVATE)

        pref.edit()
            .putInt("${uri}_start", start)
            .putInt("${uri}_end", end)
            .apply()
    }

    fun restoreLastPosition(
        context: Context,
        uri: String
    ): Pair<Int, Int> {
        val pref = context.getSharedPreferences("last_read", Context.MODE_PRIVATE)

        val s = pref.getInt("${uri}_start", -1)
        val e = pref.getInt("${uri}_end", -1)

        return s to e
    }
}
