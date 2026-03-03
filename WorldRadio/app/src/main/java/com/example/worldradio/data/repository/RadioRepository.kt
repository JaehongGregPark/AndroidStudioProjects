package com.example.worldradio.data.repository

import com.example.worldradio.data.model.RadioStation
import com.example.worldradio.data.remote.RadioApi
import com.example.worldradio.network.ServerManager
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import javax.inject.Inject

/**
 * 서버 장애 대응 + 자동 Failover 적용 Repository
 */
class RadioRepository @Inject constructor(
    private val api: RadioApi,
    private val client: OkHttpClient
) {

    /**
     * CountryCode 방식 사용 (속도 최적화)
     */
    suspend fun getStations(countryCode: String): List<RadioStation> {

        val servers = ServerManager.getShuffledServers()

        servers.forEach { baseUrl ->

            try {

                val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val newApi =
                    retrofit.create(RadioApi::class.java)

                return newApi
                    .getStationsByCountryCode(countryCode)

            } catch (e: Exception) {
                delay(500) // 다음 서버 시도 전 짧은 대기
            }
        }

        throw Exception("모든 서버 요청 실패")
    }
}