package com.example.worldradio.data.repository

import com.example.worldradio.data.local.FavoriteDao
import com.example.worldradio.data.model.FavoriteStation
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FavoriteRepository @Inject constructor(
    private val dao: FavoriteDao
) {

    fun getFavorites(): Flow<List<FavoriteStation>> =
        dao.getFavorites()

    suspend fun addFavorite(station: FavoriteStation) {
        dao.insert(station)
    }

    suspend fun removeFavorite(station: FavoriteStation) {
        dao.delete(station)
    }

    suspend fun isFavorite(url: String): Boolean {
        return dao.isFavorite(url) > 0
    }
}