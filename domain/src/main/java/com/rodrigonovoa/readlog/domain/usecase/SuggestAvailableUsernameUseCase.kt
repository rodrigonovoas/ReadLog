package com.rodrigonovoa.readlog.domain.usecase

import javax.inject.Inject
import kotlin.random.Random

class SuggestAvailableUsernameUseCase @Inject constructor(
    private val generateUsernameUseCase: GenerateUsernameUseCase,
    private val isUsernameAvailableUseCase: IsUsernameAvailableUseCase,
) {
    suspend operator fun invoke(email: String?, displayName: String?, userId: String): String {
        val seed = email?.substringBefore('@')?.ifBlank { null } ?: displayName
        val base = generateUsernameUseCase(seed, userId).take(MAX_BASE_LENGTH)

        if (isAvailable(base)) return base

        repeat(MAX_ATTEMPTS) {
            val candidate = "$base${Random.nextInt(MIN_SUFFIX, MAX_SUFFIX)}"
            if (isAvailable(candidate)) return candidate
        }

        return generateUsernameUseCase(null, userId)
    }

    private suspend fun isAvailable(candidate: String): Boolean =
        isUsernameAvailableUseCase(candidate).getOrDefault(true)

    private companion object {
        const val MAX_BASE_LENGTH = 16
        const val MAX_ATTEMPTS = 5
        const val MIN_SUFFIX = 100
        const val MAX_SUFFIX = 9999
    }
}
