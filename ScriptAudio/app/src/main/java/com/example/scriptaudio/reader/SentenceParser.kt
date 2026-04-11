package com.example.scriptaudio.reader

/**
 * SentenceParser
 *
 * 텍스트 → 문장 단위 분리
 */
object SentenceParser {

    fun parse(text: String): List<String> {

        return text

            .replace("\n", " ")

            .split(
                Regex("[.!?]")
            )

            .map { it.trim() }

            .filter { it.isNotEmpty() }

    }

}