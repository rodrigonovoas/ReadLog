package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.repository.SyncRepository
import javax.inject.Inject

class SyncUserDataUseCase @Inject constructor(
    private val syncRepository: SyncRepository,
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        return syncRepository.syncAll(userId)
    }
}
