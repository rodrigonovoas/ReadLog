package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.repository.AuthRepository
import javax.inject.Inject

open class ContinueOfflineUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    open suspend operator fun invoke(): Result<Unit> {
        return authRepository.continueOffline()
    }
}
