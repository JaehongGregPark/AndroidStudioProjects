package com.example.worldradio.ui.main

import androidx.lifecycle.*
import com.example.worldradio.data.model.Country
import com.example.worldradio.data.model.RadioStation
import com.example.worldradio.data.repository.RadioRepository
import com.example.worldradio.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.worldradio.util.CountryUtil

/**
 * 메인 화면 ViewModel
 *
 * 역할:
 * 1. 국가 입력값을 CountryCode(2자리)로 변환
 * 2. Repository 호출
 * 3. UI 상태 관리 (Loading / Success / Error)
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: RadioRepository
) : ViewModel() {

    private val _uiState =
        MutableLiveData<UiState<List<RadioStation>>>()

    val uiState: LiveData<UiState<List<RadioStation>>> =
        _uiState

    /**
     * 🔍 국가 검색
     *
     * @param country 사용자가 입력한 국가명
     */
    fun searchStations(country: String) {

        viewModelScope.launch {

            _uiState.value = UiState.Loading

            try {

                // ✅ 반드시 2자리 코드로 변환
                //val countryCode =
                //    formatCountryToCode(country)
                val countryCode =
                    CountryUtil.getCountryCode(country)
                // ✅ Repository에는 코드만 전달
                val result =
                    repository.getStations(countryCode)

                _uiState.value =
                    UiState.Success(result)

            } catch (e: Exception) {

                _uiState.value =
                    UiState.Error("검색 실패: ${e.message}")
            }
        }
    }

    /**
     * 국가 이름 → ISO 2자리 CountryCode 변환
     *
     * 예:
     * South Korea → KR
     * japan → JP
     * US → US
     */
    private fun formatCountryToCode(input: String): String {

        val trimmed = input.trim().lowercase()

        return when (trimmed) {

            // 한국
            "korea", "south korea", "대한민국" -> "KR"

            // 일본
            "japan", "일본" -> "JP"

            // 미국
            "united states", "usa", "미국" -> "US"

            // 중국
            "china", "중국" -> "CN"

            // 독일
            "germany", "독일" -> "DE"

            // 이미 2자리 코드면 그대로 사용
            else -> {
                if (trimmed.length == 2)
                    trimmed.uppercase()
                else
                    throw IllegalArgumentException(
                        "지원하지 않는 국가입니다. (2자리 코드 사용 가능)"
                    )
            }
        }
    }

    private val _countries = MutableLiveData<List<Country>>()
    val countries: LiveData<List<Country>> = _countries

    /**
     * 국가 리스트 불러오기
     */
    fun loadCountries() {

        viewModelScope.launch {

            try {
                val result = repository.getCountries()

                // 방송국 없는 국가는 제거
                _countries.value = result.filter { it.stationcount > 0 }

            } catch (e: Exception) {
                _countries.value = emptyList()
            }
        }
    }
}