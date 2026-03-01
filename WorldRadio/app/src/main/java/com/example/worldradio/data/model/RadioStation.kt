package com.example.worldradio.data.model

/**
 * 라디오 방송국 데이터 모델
 */
data class RadioStation(
    val stationuuid: String,
    val name: String,
    val url: String,
    val favicon: String?,
    val country: String?
)