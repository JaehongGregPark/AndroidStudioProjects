package com.example.worldradio.ui.main

import androidx.lifecycle.*
import com.example.worldradio.data.model.Country
import com.example.worldradio.data.model.FavoriteStation
import com.example.worldradio.data.model.RadioStation
import com.example.worldradio.data.repository.RadioRepository
import com.example.worldradio.data.repository.FavoriteRepository
import com.example.worldradio.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.worldradio.util.CountryUtil

/**
 * 메인 화면 ViewModel
 *
 * 역할
 * 1️⃣ 국가 입력값 → ISO 2자리 코드 변환
 * 2️⃣ Repository 호출
 * 3️⃣ UI 상태 관리 (Loading / Success / Error)
 * 4️⃣ 즐겨찾기 저장 / 로드
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: RadioRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    /**
     * 방송국 목록 UI 상태
     */
    private val _uiState =
        MutableLiveData<UiState<List<RadioStation>>>()

    val uiState: LiveData<UiState<List<RadioStation>>> =
        _uiState


    /**
     * 🔍 국가 검색
     *
     * 사용자가 입력한 국가 이름을
     * ISO 2자리 코드로 변환 후 API 호출
     */
    fun searchStations(country: String) {

        viewModelScope.launch {

            _uiState.value = UiState.Loading

            try {

                // 국가 이름 → 국가 코드
                val countryCode =
                    CountryUtil.getCountryCode(country)

                // Repository 호출
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
     * 국가 목록 LiveData
     */
    private val _countries = MutableLiveData<List<Country>>()
    val countries: LiveData<List<Country>> = _countries


    /**
     * 🌍 국가 목록 로드
     *
     * RadioBrowser API에서 국가 목록 조회
     * 방송국 없는 국가는 제외
     */
    fun loadCountries() {

        viewModelScope.launch {

            try {

                val result = repository.getCountries()

                val filteredList = result
                    .filter { it.stationcount > 0 }
                    .sortedBy { it.name }

                _countries.value = filteredList

            } catch (e: Exception) {

                _countries.value = emptyList()
            }
        }
    }


    /**
     * ⭐ 즐겨찾기 추가
     */
    fun toggleFavorite(station: RadioStation) {

        viewModelScope.launch {

            val fav = FavoriteStation(

                stationuuid = station.stationuuid,   // ⭐ 추가

                url = station.urlResolved,

                name = station.name,

                country = station.country ?: "",

                favicon = station.favicon
            )

            favoriteRepository.addFavorite(fav)
        }
    }


    /**
     * ⭐ 즐겨찾기 목록 로드
     *
     * Room DB → FavoriteStation
     * FavoriteStation → RadioStation 변환
     */
    fun loadFavorites() {

        viewModelScope.launch {

            favoriteRepository.getFavorites().collect { favorites ->

                val stations = favorites.map {

                    RadioStation(
                        stationuuid = it.stationuuid,
                        name = it.name,
                        country = it.country,
                        favicon = it.favicon,
                        url = it.url,
                        urlResolved = it.url

                    )
                }

                _uiState.value = UiState.Success(stations)
            }
        }
    }
}