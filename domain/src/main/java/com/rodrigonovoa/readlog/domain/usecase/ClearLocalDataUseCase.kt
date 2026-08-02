package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.repository.LocalDataRepository
import javax.inject.Inject

class ClearLocalDataUseCase @Inject constructor(
    private val localDataRepository: LocalDataRepository,
) {
    suspend operator fun invoke() {
        localDataRepository.clearAllLocalData()
    }
}
