package com.rodrigonovoa.readlog.domain.usecase

import com.rodrigonovoa.readlog.domain.model.UserProfileInfo
import com.rodrigonovoa.readlog.domain.repository.UserProfileRepository
import javax.inject.Inject

class GetRemoteUserProfileInfoUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
) {
    suspend operator fun invoke(userId: String): Result<UserProfileInfo> {
        return userProfileRepository.getRemoteUserProfileInfo(userId)
    }
}
