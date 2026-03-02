package com.example.worldradio.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.worldradio.data.model.FavoriteStation

// Room Database 클래스
// entities 배열에 테이블 클래스 지정
@Database(
    entities = [FavoriteStation::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAO 반환 함수
    abstract fun favoriteDao(): FavoriteDao

    companion object {

        // 싱글톤 인스턴스 (앱 전체에서 하나만 사용)
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            // 기존 인스턴스가 있으면 반환
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "radio_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}