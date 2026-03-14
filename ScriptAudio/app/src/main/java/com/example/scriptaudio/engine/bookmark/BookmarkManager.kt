package com.example.scriptaudio.engine.bookmark

/**
 * BookmarkManager
 *
 * 리더 북마크 관리
 */

data class Bookmark(

    val filePath: String,

    val page: Int,

    val note: String = ""

)

class BookmarkManager {

    private val bookmarks = mutableListOf<Bookmark>()

    /**
     * 북마크 추가
     */
    fun addBookmark(

        filePath: String,
        page: Int

    ) {

        bookmarks.add(
            Bookmark(filePath, page)
        )

    }

    /**
     * 북마크 조회
     */
    fun getBookmarks(): List<Bookmark> {

        return bookmarks

    }

    /**
     * 북마크 삭제
     */
    fun removeBookmark(bookmark: Bookmark) {

        bookmarks.remove(bookmark)

    }

}