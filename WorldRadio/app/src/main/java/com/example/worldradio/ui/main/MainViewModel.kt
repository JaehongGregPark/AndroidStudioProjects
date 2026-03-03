package com.example.worldradio.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.worldradio.data.model.RadioStation
import com.example.worldradio.data.repository.RadioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: RadioRepository
) : ViewModel() {


    // 내부 수정용
    private val _stations = MutableLiveData<List<RadioStation>>()

    // 외부 공개용 (읽기 전용)
    val stations: LiveData<List<RadioStation>> = _stations

    /**
     * 검색 실행 함수
     */
    fun searchStations(country: String) {

        viewModelScope.launch {

            try {
                val result =
                    repository.getStations(country.lowercase())

                _stations.value = result

            } catch (e: Exception) {
                // 네트워크 실패 시 빈 리스트 반환
                _stations.value = emptyList()
            }
        }
    }
}