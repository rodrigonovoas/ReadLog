package com.rodrigonovoa.readlog.domain.repository

import com.rodrigonovoa.readlog.domain.model.UserProfileInfo

interface UserProfileRepository {
    suspend fun getUserProfileInfo(userId: String): UserProfileInfo
    suspend fun refreshUserProfileInfo(
        userId: String,
        startOfMonthMillis: Long,
        displayName: String?,
    ): Result<UserProfileInfo>
    suspend fun getRemoteUserProfileInfo(userId: String): Result<UserProfileInfo>
    suspend fun setUsername(userId: String, username: String): Result<UserProfileInfo>
    suspend fun setLiked(currentUserId: String, targetUserId: String, liked: Boolean): Result<Unit>
    suspend fun getLikedProfiles(currentUserId: String): Result<List<UserProfileInfo>>
}
