package com.example.worldradio.data.remote

import com.example.worldradio.data.model.RadioStation
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Radio Browser REST API 인터페이스
 *
 * Retrofit이 이 인터페이스를 기반으로
 * 실제 네트워크 구현체를 자동 생성한다.
 *
 * suspend 키워드:
 *  → 코루틴에서 비동기 네트워크 호출을 가능하게 함
 */
interface RadioApi {

    /**
     * 국가별 방송국 목록 조회
     *
     * @GET("stations/bycountry")
     *  → baseUrl 뒤에 붙는 endpoint
     *
     * @Query("country")
     *  → URL 쿼리 파라미터로 자동 변환
     *     예:
     *     stations/bycountry?country=korea
     *
     * @Query("limit")
     *  → 최대 결과 개수 제한
     */
    @GET("stations/bycountrycodeexact")
    suspend fun getStationsByCountryCode(
        @Query("countrycode") countryCode: String,
        @Query("limit") limit: Int = 50
    ): List<RadioStation>
}