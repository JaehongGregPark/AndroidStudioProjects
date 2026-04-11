package com.example.worldradio.util

import android.content.Context

/**
 * 최근 검색 국가 저장 클래스
 *
 * SharedPreferences 사용
 */
class RecentSearchManager(context: Context) {

    private val prefs =
        context.getSharedPreferences("recent_search", Context.MODE_PRIVATE)

    private val KEY = "countries"

    /**
     * 최근 검색 저장
     */
    fun saveCountry(country: String) {

        val list = getRecent().toMutableList()
        list.remove(country)
        list.add(0, country)
        if (list.size > 5) list.removeLast()
        prefs.edit().putString(KEY, list.joinToString(",")).apply()
    }

    /**
     * 최근 검색 리스트 반환
     */
    fun getRecent(): List<String> {
        return prefs.getString(KEY, "")
            ?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }
}