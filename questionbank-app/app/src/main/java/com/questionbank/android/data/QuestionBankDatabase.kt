package com.questionbank.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [QuestionEntity::class, ChoiceEntity::class],
    version = 2,
    exportSchema = false
)
abstract class QuestionBankDatabase : RoomDatabase() {

    abstract fun questionDao(): QuestionDao

    companion object {
        @Volatile
        private var instance: QuestionBankDatabase? = null

        fun getInstance(context: Context): QuestionBankDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    QuestionBankDatabase::class.java,
                    "question_bank.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
