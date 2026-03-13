package com.example.worldradio.data.model

/**
 * 국가 정보 모델
 *
 * name : 국가 이름
 * code : RadioBrowser API 국가 코드
 * flagRes : 국기 drawable
 */
data class Country(
    val name: String,

    val iso_3166_1: String,

    val stationcount: Int,

    val flagRes: Int
)