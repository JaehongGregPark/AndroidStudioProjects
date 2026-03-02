package com.example.worldradio.data.repository

import com.example.worldradio.data.local.FavoriteDao
import com.example.worldradio.data.model.FavoriteStation
import com.example.worldradio.data.model.RadioStation
import com.example.worldradio.data.remote.RadioApi
import javax.inject.Inject

/**
 * Repository 계층
 *
 * 역할:
 * - 네트워크(API)와 로컬 DB(Room)를 한 곳에서 관리
 * - ViewModel은 여기만 바라본다
 * - 데이터 출처(API/DB)를 숨긴다 (캡슐화)
 *
 * DI(Hilt)로 api, dao를 주입받는다
 */
class RadioRepository @Inject constructor(

    // 🌐 네트워크 API
    private val api: RadioApi,

    // 💾 Room DAO
    private val dao: FavoriteDao

) {

    // ==============================
    // 🌐 네트워크 관련
    // ==============================

    /**
     * 전체 인기 라디오 목록 요청
     */
    suspend fun getStations(): List<RadioStation> {
        return api.getTopStations()
    }

    /**
     * 국가별 라디오 목록 요청
     */
    suspend fun getStations(country: String): List<RadioStation> {
        return api.getStationsByCountry(country)
    }

    // ==============================
    // ❤️ 즐겨찾기 (Room DB)
    // ==============================

    /**
     * 즐겨찾기 추가
     */
    suspend fun addFavorite(station: FavoriteStation) {
        dao.insert(station)
    }

    /**
     * 즐겨찾기 제거
     */
    suspend fun removeFavorite(station: FavoriteStation) {
        dao.delete(station)
    }

    /**
     * 특정 URL이 즐겨찾기인지 확인
     */
    suspend fun isFavorite(url: String): Boolean {
        return dao.isFavorite(url)
    }

    /**
     * 전체 즐겨찾기 목록 조회
     */
    suspend fun getFavorites(): List<FavoriteStation> {
        return dao.getAll()
    }
}