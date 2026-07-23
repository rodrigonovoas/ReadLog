package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.repository.UserProfileRepository
import java.io.IOException
import javax.inject.Inject

class ToggleUserLikeUseCase @Inject constructor(
    private val isOnlineUseCase: IsOnlineUseCase,
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke(currentUserId: String, targetUserId: String, liked: Boolean): Result<Unit> {
        if (!isOnlineUseCase()) {
            return Result.failure(IOException("No internet connection"))
        }
        return userProfileRepository.setLiked(currentUserId, targetUserId, liked)
    }
}
