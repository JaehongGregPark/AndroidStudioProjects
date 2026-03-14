package com.example.scriptaudio.search
// ↑ 파일 검색 기능 패키지

import java.io.File
// ↑ File 클래스

/**
 * FileSearch
 *
 * 파일 목록에서 이름으로 검색하는 유틸리티 클래스
 *
 * 예
 *
 * novel.txt
 * book.pdf
 * story.txt
 */
object FileSearch {

    /**
     * 파일 검색 함수
     *
     * @param files 전체 파일 목록
     * @param query 검색 문자열
     *
     * @return 검색된 파일 목록
     */
    fun search(

        files: List<File>,

        query: String

    ): List<File> {

        /**
         * 검색어가 비어 있으면
         * 전체 파일 반환
         */
        if (query.isBlank()) {

            return files

        }

        /**
         * 파일 이름 기준 필터링
         */
        return files.filter { file ->

            file.name.contains(

                query,

                ignoreCase = true

            )

        }

    }

}