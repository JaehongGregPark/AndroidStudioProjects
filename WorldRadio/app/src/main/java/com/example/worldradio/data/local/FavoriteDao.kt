package com.example.worldradio.data.local

import androidx.room.*
import com.example.worldradio.data.model.FavoriteStation
import kotlinx.coroutines.flow.Flow

// DAO = Database Access Object
// 실제 DB와 통신하는 인터페이스
@Dao
interface FavoriteDao {

    // 즐겨찾기 추가
    // 이미 존재하면 덮어쓰기
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(station: FavoriteStation)

    // 즐겨찾기 삭제
    @Delete
    suspend fun delete(station: FavoriteStation)

    // 전체 즐겨찾기 목록 가져오기
    @Query("SELECT * FROM favorite_stations")
    fun getFavorites(): Flow<List<FavoriteStation>>

    // 특정 URL이 즐겨찾기인지 확인
    @Query("SELECT COUNT(*) FROM favorite_stations  WHERE url = :url")
    suspend fun isFavorite(url: String): Int
}