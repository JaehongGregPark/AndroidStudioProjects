package com.example.worldradio.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room 테이블 정의
@Entity(tableName = "favorite_stations")
data class FavoriteStation(

    // 스트림 URL을 PrimaryKey로 사용
    // 동일 URL 중복 저장 방지
    @PrimaryKey
    val url: String,

    val name: String,
    val country: String,
    val favicon: String?
)