package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import com.rodrigonovoa.readlog.domain.repository.UserProfileRepository
import javax.inject.Inject

class SetUserSearchVisibilityUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke(userId: String, hidden: Boolean): Result<UserProfileInfo> {
        return userProfileRepository.setHiddenFromSearch(userId, hidden)
    }
}
