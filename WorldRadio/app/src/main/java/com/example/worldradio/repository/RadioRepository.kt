package com.example.worldradio.repository

import com.example.worldradio.data.model.RadioStation
import com.example.worldradio.data.remote.RadioApi
import javax.inject.Inject

// 데이터 소스(API)와 ViewModel 사이를 연결하는 계층
/**
 * Repository 계층
 *
 * ViewModel은 네트워크 구현을 몰라야 한다.
 * 데이터 접근을 중간에서 추상화한다.
 */
class RadioRepository @Inject constructor(
    private val api: RadioApi
) {

    /**
     * 서버에서 라디오 목록 요청
     */
    suspend fun getStations(): List<RadioStation> {
        return api.getTopStations()
    }
}