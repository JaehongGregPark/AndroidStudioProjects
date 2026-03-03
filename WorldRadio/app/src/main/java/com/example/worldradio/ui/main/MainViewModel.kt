package com.example.worldradio.ui.main

import androidx.lifecycle.*
import com.example.worldradio.data.model.RadioStation
import com.example.worldradio.data.repository.RadioRepository
import com.example.worldradio.ui.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: RadioRepository
) : ViewModel() {

    private val _uiState =
        MutableLiveData<UiState<List<RadioStation>>>()

    val uiState: LiveData<UiState<List<RadioStation>>> = _uiState

    /**
     * 국가 검색
     */
    fun searchStations(country: String) {

        viewModelScope.launch {

            _uiState.value = UiState.Loading

            try {

                val formattedCountry = formatCountry(country)

                val result =
                    repository.getStations(formattedCountry)

                _uiState.value =
                    UiState.Success(result)

            } catch (e: Exception) {

                _uiState.value =
                    UiState.Error("검색 실패: ${e.message}")
            }
        }
    }

    /**
     * 국가명 변환
     */
    private fun formatCountry(input: String): String {

        val trimmed = input.trim().lowercase()

        return when (trimmed) {
            "korea" -> "South Korea"
            else -> trimmed.replaceFirstChar { it.uppercase() }
        }
    }

    private fun formatCountryToCode(input: String): String {

        return when (input.trim().lowercase()) {
            "korea", "south korea" -> "KR"
            "japan" -> "JP"
            "united states", "usa" -> "US"
            else -> input.uppercase()
        }
    }
}