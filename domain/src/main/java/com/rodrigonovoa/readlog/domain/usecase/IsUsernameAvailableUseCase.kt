package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.repository.UserSearchRepository
import javax.inject.Inject

class IsUsernameAvailableUseCase @Inject constructor(
    private val userSearchRepository: UserSearchRepository,
) {
    suspend operator fun invoke(username: String): Result<Boolean> {
        val normalized = username.trim().lowercase()
        return userSearchRepository.isUsernameTaken(normalized).map { taken -> !taken }
    }
}
