package com.example.audioscript.data.repository

import android.content.Context

import com.example.audioscript.data.ml.MLKitTranslator

import com.example.audioscript.domain.repository.TextRepository

import com.google.mlkit.nl.translate.TranslateLanguage

import dagger.hilt.android.qualifiers.ApplicationContext

import java.io.File

import javax.inject.Inject


/**
 * ===============================
 * Data Layer - Repository 구현체
 * ===============================
 *
 * 역할:
 *
 * ✔ Domain Interface 구현
 * ✔ 실제 기능 수행
 *
 * 포함 기능:
 *
 * - 번역
 * - 소설 생성
 * - TXT 저장
 * - 텍스트 로드
 *
 * Domain Layer는 이 클래스 존재를 모른다.
 */
class TextRepositoryImpl @Inject constructor(

    /**
     * MLKit 번역기
     */
    private val translator: MLKitTranslator,


    /**
     * 파일 저장을 위한 Context
     */
    @ApplicationContext
    private val context: Context


) : TextRepository {


    /**
     * 초기 텍스트 로드
     *
     * 현재:
     * 빈 문자열
     *
     * 향후:
     * Room DB 연결
     */
    override suspend fun loadText(): String {

        return ""

    }


    /**
     * 번역 기능
     *
     * 한글 포함 여부로 방향 결정
     */
    override suspend fun translate(

        text: String

    ): String {


        val isKorean =
            text.contains(Regex("[가-힣]"))


        return if (isKorean) {

            translator.translate(

                text,

                TranslateLanguage.KOREAN,

                TranslateLanguage.ENGLISH

            )

        } else {

            translator.translate(

                text,

                TranslateLanguage.ENGLISH,

                TranslateLanguage.KOREAN

            )

        }

    }


    /**
     * 소설 생성
     *
     * 현재:
     * 더미 데이터
     */
    override suspend fun generateStory(

        title: String,

        isKorean: Boolean

    ): String {


        val base =

            if (isKorean)

                "그날의 기억은 아직도 생생하다..."

            else

                "The memory of that day still lingers..."



        return buildString {


            append(title)

            append("\n\n")


            repeat(20) {

                append(base)

                append("\n\n")

            }

        }

    }


    /**
     * TXT 파일 저장 기능
     *
     * 저장 위치:
     *
     * /data/data/패키지/files/
     *
     * 파일 앱에서 확인 가능
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