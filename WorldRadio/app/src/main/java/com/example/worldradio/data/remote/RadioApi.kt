package com.example.worldradio.data.remote

import com.example.worldradio.data.model.RadioStation
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Radio Browser REST API 인터페이스
 *
 * 국가 이름으로 검색하도록 수정
 * 예:
 * stations/bycountry/Korea?limit=50
 */
interface RadioApi {

    /**
     * 국가 이름으로 방송국 조회
     *
     * @Path("country")
     *  → URL 경로에 국가명 삽입
     *
     * @Query("limit")
     *  → 최대 결과 개수 제한
     */
    @GET("stations/bycountry/{country}")
    suspend fun getStationsByCountry(
        @Path("country") country: String,
        @Query("limit") limit: Int = 50
    ): List<RadioStation>
    /**
     * 가장 빠른 방식
     *
     * 예:
     * stations/bycountrycodeexact/KR
     */
    @GET("stations/bycountrycodeexact/{code}")
    suspend fun getStationsByCountryCode(
        @Path("code") countryCode: String,
        @Query("limit") limit: Int = 50
    ): List<RadioStation>
}