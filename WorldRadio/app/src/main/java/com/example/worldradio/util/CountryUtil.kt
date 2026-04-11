package com.example.worldradio.util

import java.util.*

/**
 * ISO 국가코드 자동 변환 유틸
 *
 * - 200개 국가 자동 지원
 * - Locale 기반 처리
 */
object CountryUtil {

    /**
     * 국가명 → ISO 2자리 코드 변환
     */
    fun getCountryCode(input: String): String {

        val trimmed = input.trim()

        // 이미 2자리 코드면 그대로 반환
        if (trimmed.length == 2) {
            return trimmed.uppercase()
        }

        val locales = Locale.getAvailableLocales()

        for (locale in locales) {

            val countryNameEn = locale.displayCountry
            val countryNameLocal =
                locale.getDisplayCountry(Locale.getDefault())

            if (trimmed.equals(countryNameEn, true) ||
                trimmed.equals(countryNameLocal, true)
            ) {
                return locale.country
            }
        }

        throw IllegalArgumentException("지원하지 않는 국가입니다.")
    }

    /**
     * 전체 국가 리스트 반환 (자동완성용)
     */
    fun getAllCountries(): List<String> {

        return Locale.getISOCountries().map {
            Locale("", it).displayCountry
        }.distinct().sorted()
    }
}