package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import javax.inject.Inject

sealed interface ClaimUsernameResult {
    data class Success(val profile: UserProfileInfo) : ClaimUsernameResult
    data object InvalidFormat : ClaimUsernameResult
    data object AlreadyTaken : ClaimUsernameResult
    data class Error(val throwable: Throwable) : ClaimUsernameResult
}

class ClaimUsernameUseCase @Inject constructor(
    private val isUsernameAvailableUseCase: IsUsernameAvailableUseCase,
    private val setUsernameUseCase: SetUsernameUseCase,
) {
    suspend operator fun invoke(userId: String, rawUsername: String): ClaimUsernameResult {
        val candidate = rawUsername.trim()
        if (!USERNAME_REGEX.matches(candidate)) return ClaimUsernameResult.InvalidFormat

        val availability = isUsernameAvailableUseCase(candidate)
        if (availability.isFailure) {
            return ClaimUsernameResult.Error(availability.exceptionOrNull()!!)
        }
        if (!availability.getOrThrow()) return ClaimUsernameResult.AlreadyTaken

        return setUsernameUseCase(userId, candidate).fold(
            onSuccess = { ClaimUsernameResult.Success(it) },
            onFailure = { ClaimUsernameResult.Error(it) },
        )
    }

    private companion object {
        val USERNAME_REGEX = Regex("^[a-zA-Z0-9_]{3,20}$")
    }
}
