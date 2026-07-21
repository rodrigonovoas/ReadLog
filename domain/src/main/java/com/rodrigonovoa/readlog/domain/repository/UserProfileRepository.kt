package com.rodrigonovoa.readlog.domain.repository

import com.rodrigonovoa.readlog.domain.model.UserProfileInfo

interface UserProfileRepository {
    suspend fun getUserProfileInfo(userId: String): UserProfileInfo
    suspend fun refreshUserProfileInfo(
        userId: String,
        startOfWeekMillis: Long,
        displayName: String?,
    ): Result<UserProfileInfo>
    suspend fun getRemoteUserProfileInfo(userId: String): Result<UserProfileInfo>
}
