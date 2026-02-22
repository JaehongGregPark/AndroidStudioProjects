package com.example.audioscript.data.remote

import retrofit2.http.Body
import retrofit2.http.POST


/**
 * ===============================
 * LibreTranslate Request Body
 * ===============================
 *
 * LibreTranslate 서버에 번역 요청 시
 * JSON 형태로 전달되는 데이터 모델
 *
 * 예시 JSON:
 *
 * {
 *   "q": "안녕하세요",
 *   "source": "ko",
 *   "target": "en",
 *   "format": "text"
 * }
 *
 */
data class LibreRequest(

    /**
     * 번역할 원본 텍스트
     */
    val q: String,

    /**
     * 원본 언어 코드
     *
     * 예:
     * ko = 한국어
     * en = 영어
     * ja = 일본어
     */
    val source: String,

    /**
     * 번역할 대상 언어 코드
     */
    val target: String,

    /**
     * 텍스트 형식
     *
     * text = 일반 텍스트
     * html = HTML 형식
     *
     * 기본값은 text 사용
     */
    val format: String = "text"

)



/**
 * ===============================
 * LibreTranslate Response Body
 * ===============================
 *
 * 서버에서 응답하는 JSON:
 *
 * {
 *   "translatedText": "Hello"
 * }
 *
 */
data class LibreResponse(

    /**
     * 번역 결과 텍스트
     */
    val translatedText: String

)



/**
 * ===============================
 * LibreTranslate API Interface
 * ===============================
 *
 * Retrofit에서 사용하는 인터페이스
 *
 * 실제 호출 주소:
 *
 * POST
 * https://libretranslate.de/translate
 *
 */
interface LibreTranslateApi {


    /**
     * 번역 요청 함수
     *
     * suspend:
     * 코루틴에서 실행 가능
     *
     * @Body:
     * JSON Body로 전달됨
     *
     */
    @POST("translate")
    suspend fun translate(

        @Body request: LibreRequest

    ): LibreResponse


}