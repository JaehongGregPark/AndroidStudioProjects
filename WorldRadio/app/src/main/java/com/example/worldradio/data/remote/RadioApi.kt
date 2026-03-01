package com.example.worldradio.data.remote

import com.example.worldradio.data.model.RadioStation
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Radio Browser API 인터페이스
 *
 * suspend 키워드를 사용해
 * 코루틴 기반 비동기 네트워크 호출
 */
interface RadioApi {

    /**
     * 상위 클릭 방송 50개 가져오기
     */
    @GET("stations/topclick/50")
    suspend fun getTopStations(): List<RadioStation>


    @GET("stations/bycountry/{country}")
    suspend fun getStationsByCountry(
        @Path("country") country: String
    ): List<RadioStation>
}