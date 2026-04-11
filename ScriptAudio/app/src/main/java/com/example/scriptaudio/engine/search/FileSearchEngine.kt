package com.example.scriptaudio.engine.search

/**
 * FileSearchEngine
 *
 * ScriptAudio 파일 검색 엔진
 *
 * 기능
 * - 파일 이름 검색
 * - 텍스트 내부 검색
 */

import java.io.File

class FileSearchEngine {

    /**
     * 파일 이름 검색
     */

    fun searchFiles(

        files: List<File>,

        keyword: String

    ): List<File> {

        return files.filter {

            it.name.contains(
                keyword,
                ignoreCase = true
            )

        }

    }

    /**
     * 파일 내용 검색
     */

    fun searchText(

        text: String,

        keyword: String

    ): List<Int> {

        val results = mutableListOf<Int>()

        var index = text.indexOf(keyword)

        while (index >= 0) {

            results.add(index)

            index = text.indexOf(
                keyword,
                index + keyword.length
            )

        }

        return results

    }

}