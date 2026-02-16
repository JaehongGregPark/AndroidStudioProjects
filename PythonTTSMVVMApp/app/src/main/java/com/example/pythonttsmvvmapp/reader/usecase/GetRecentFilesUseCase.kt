package com.example.pythonttsmvvmapp.reader.usecase

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.pythonttsmvvmapp.data.parser.PdfParser
import com.example.pythonttsmvvmapp.data.parser.TxtParser
import com.example.pythonttsmvvmapp.reader.data.repository.ReaderRepository
import javax.inject.Inject

class GetRecentFilesUseCase @Inject constructor(
    private val repository: RecentRepository
) {
    suspend operator fun invoke(context: Context): List<Pair<String, String>> {
        return repository.getRecentFiles(context)
    }
}