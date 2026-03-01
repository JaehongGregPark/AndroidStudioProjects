package com.example.worldradio.viewmodel

import androidx.lifecycle.*
import com.example.worldradio.repository.RadioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.worldradio.data.model.RadioStation


/**
 * MVVM 구조의 ViewModel
 *
 * UI와 데이터를 연결하는 역할
 */
@HiltViewModel
class RadioViewModel @Inject constructor(
    private val repository: RadioRepository
) : ViewModel() {

    private val _stations = MutableLiveData<List<RadioStation>>()
    val stations: LiveData<List<RadioStation>> = _stations

    fun searchStations() {
        viewModelScope.launch {
            try {
                _stations.value = repository.getStations()
            } catch (e: Exception) {
                _stations.value = emptyList()
            }
        }
    }
}