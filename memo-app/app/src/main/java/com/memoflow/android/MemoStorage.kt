package com.memoflow.android

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class MemoStorage(context: Context) {
    private val prefs = context.getSharedPreferences("memo_flow_storage", Context.MODE_PRIVATE)

    fun loadMemos(): MutableList<Memo> {
        val raw = prefs.getString(KEY_MEMOS, null) ?: return mutableListOf()
        val jsonArray = JSONArray(raw)
        val memos = mutableListOf<Memo>()

        for (index in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(index)
            memos.add(
                Memo(
                    id = item.getString("id"),
                    title = item.getString("title"),
                    content = item.getString("content"),
                    createdAt = item.getLong("createdAt"),
                    updatedAt = item.optLong("updatedAt").takeIf { item.has("updatedAt") }
                )
            )
        }

        return memos
    }

    fun saveMemos(memos: List<Memo>) {
        val jsonArray = JSONArray()
        memos.forEach { memo ->
            jsonArray.put(
                JSONObject().apply {
                    put("id", memo.id)
                    put("title", memo.title)
                    put("content", memo.content)
                    put("createdAt", memo.createdAt)
                    memo.updatedAt?.let { put("updatedAt", it) }
                }
            )
        }

        prefs.edit().putString(KEY_MEMOS, jsonArray.toString()).apply()
    }

    private companion object {
        const val KEY_MEMOS = "memos"
    }
}
