package com.example.audioscript.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


/**
 * ===============================
 * Retrofit Instance
 * ===============================
 *
 * Retrofit:
 * HTTP 통신 라이브러리
 *
 * 역할:
 *
 * ✔ 서버 연결
 * ✔ JSON 변환
 * ✔ API 호출 객체 생성
 *
 */
object RetrofitInstance {


    /**
     * LibreTranslate API 객체 생성
     *
     * lazy:
     * 최초 1회만 생성
     */
    val api: LibreTranslateApi by lazy {


        Retrofit.Builder()

            /**
             * 서버 Base URL
             *
             * 중요:
             * 반드시 "/" 로 끝나야 함
             */
            .baseUrl("https://libretranslate.de/")


            /**
             * JSON → Kotlin 객체 변환
             */
            .addConverterFactory(

                GsonConverterFactory.create()

            )


            /**
             * Retrofit 객체 생성
             */
            .build()


            /**
             * API 인터페이스 연결
             */
            .create(LibreTranslateApi::class.java)


    }


}