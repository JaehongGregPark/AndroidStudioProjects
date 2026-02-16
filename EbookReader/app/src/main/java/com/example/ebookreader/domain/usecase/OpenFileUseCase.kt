package com.example.ebookreader.domain.usecase

import android.content.Context
import android.net.Uri
import com.example.ebookreader.domain.repository.ReaderRepository
import javax.inject.Inject

/**
 * 파일 열기 유스케이스
 *
 * UI → ViewModel → UseCase → Repository
 */
class OpenFileUseCase @Inject constructor(
    private val repository: ReaderRepository
) {

    suspend operator fun invoke(
        context: Context,
        uri: Uri
    ) = repository.openFile(context, uri)
}
