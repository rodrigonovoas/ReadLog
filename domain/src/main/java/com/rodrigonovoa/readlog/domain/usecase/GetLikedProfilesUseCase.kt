package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import com.rodrigonovoa.readlog.domain.repository.UserProfileRepository
import java.io.IOException
import javax.inject.Inject

class GetLikedProfilesUseCase @Inject constructor(
    private val isOnlineUseCase: IsOnlineUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke(): Result<List<UserProfileInfo>> {
        if (!isOnlineUseCase()) {
            return Result.failure(IOException("No internet connection"))
        }
        val currentUserId = getCurrentUserUseCase()?.uid
            ?: return Result.success(emptyList())
        return userProfileRepository.getLikedProfiles(currentUserId)
    }
}
