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

    private val _stations = MutableLiveData<List<RadioStation>>()
    val stations: LiveData<List<RadioStation>> = _stations

    fun searchStations(country: String) {

        viewModelScope.launch {

            try {
                val result = repository.getStations(country)
                _stations.value = result

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}