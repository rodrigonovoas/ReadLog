package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.repository.AuthRepository
import javax.inject.Inject

class GetUserDisplayNameUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): String {
        val rawName = authRepository.getCurrentUser()?.displayName?.ifBlank { null }
        return rawName?.split(" ")?.firstOrNull() ?: "reader"
    }
}
