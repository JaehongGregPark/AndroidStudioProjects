package com.example.audioscript.data.repository

import android.content.Context

import com.example.audioscript.domain.repository.TextRepository

import dagger.hilt.android.qualifiers.ApplicationContext

import java.io.File

import javax.inject.Inject


class TextRepositoryImpl @Inject constructor(

    @ApplicationContext
    private val context: Context

) : TextRepository {


    /**
     * 텍스트 불러오기
     */
    override suspend fun loadText(): String {

        return try {

            val file = File(
                context.filesDir,
                "GeneratedStory.txt"
            )

            if (file.exists()) {

                file.readText()

            } else {

                ""

            }

        } catch (e: Exception) {

            ""

        }

    }



    /**
     * 번역
     */
    override suspend fun translate(

        text: String

    ): String {

        // TODO 실제 번역 API 연결

        return "번역됨: $text"

    }



    /**
     * 소설 생성
     */
    override suspend fun generateStory(

        title: String,

        isKorean: Boolean

    ): String {

        return if (isKorean) {

            "한국어 소설: $title"

        } else {

            "English Story: $title"

        }

    }



    /**
     * TXT 저장
     */
    override suspend fun exportTxt(

        fileName: String,

        content: String

    ) {

        val file = File(

            context.filesDir,

            "$fileName.txt"

        )

        file.writeText(content)

    }

}
