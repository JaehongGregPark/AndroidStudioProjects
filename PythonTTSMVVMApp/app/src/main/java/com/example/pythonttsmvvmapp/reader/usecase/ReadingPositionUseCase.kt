package com.example.pythonttsmvvmapp.reader.usecase

import android.content.Context
import com.example.pythonttsmvvmapp.reader.data.repository.ReaderRepository
import javax.inject.Inject
/**
class ReadingPositionUseCase @Inject constructor() {

    fun save(
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

    fun restore(
        context: Context,
        uri: String
    ): Pair<Int, Int> {
        val pref = context.getSharedPreferences("last_read", Context.MODE_PRIVATE)

        val s = pref.getInt("${uri}_start", -1)
        val e = pref.getInt("${uri}_end", -1)

        return s to e
    }
}
*/
class ReadingPositionUseCase @Inject constructor(
    private val repository: ReaderRepository
) {
    fun save(context: Context, uri: String, s: Int, e: Int) =
        repository.saveLastPosition(context, uri, s, e)

    fun restore(context: Context, uri: String) =
        repository.restoreLastPosition(context, uri)
}