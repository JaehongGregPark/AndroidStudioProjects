package com.example.scriptaudio.data.local

/**
 * BookmarkDatabase
 *
 * Room DB 생성
 */

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(

    entities = [

        BookmarkEntity::class

    ],

    version = 1

)

abstract class BookmarkDatabase :

    RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao

}