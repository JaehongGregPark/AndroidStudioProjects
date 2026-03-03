package com.example.worldradio.data.repository

import com.example.worldradio.data.model.RadioStation
import com.example.worldradio.data.remote.RadioApi
import kotlinx.coroutines.delay
import retrofit2.HttpException
import javax.inject.Inject

/**
 * Repository 계층
 *
 * 역할:
 *  - 데이터 소스(API)와 ViewModel 사이 중간 계층
 *  - 네트워크 예외 처리
 *  - 재시도 로직 처리
 */
class RadioRepository @Inject constructor(
    private val api: RadioApi
) {

    /**
     * 국가별 방송 검색
     *
     * repeat(3)
     *  → 최대 3번 재시도
     *
     * 502 Bad Gateway 등 서버 일시 오류 대응
     */
    suspend fun getStations_(country: String): List<RadioStation> {

        repeat(3) { attempt ->

            try {
                // API 호출
                //return api.getStationsByCountry(country)

            } catch (e: HttpException) {

                // 마지막 시도라면 예외 그대로 던짐
                if (attempt == 2) throw e

                // 1초 대기 후 재시도
                delay(1000)
            }
        }

        return emptyList()
    }

    suspend fun getStations(countryCode: String): List<RadioStation> {
        return api.getStationsByCountryCode(countryCode)
    }
}