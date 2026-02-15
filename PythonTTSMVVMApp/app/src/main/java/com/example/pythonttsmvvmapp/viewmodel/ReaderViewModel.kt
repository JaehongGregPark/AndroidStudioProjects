package com.example.pythonttsmvvmapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pythonttsmvvmapp.data.entity.ReadingFile
import com.example.pythonttsmvvmapp.data.repository.ReadingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI 상태 관리
 */
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val repo: ReadingRepository
) : ViewModel() {

    fun save(file: ReadingFile) {
        viewModelScope.launch {
            repo.save(file)
        }
    }
}