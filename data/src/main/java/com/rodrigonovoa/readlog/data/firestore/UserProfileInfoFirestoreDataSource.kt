package com.rodrigonovoa.readlog.data.firestore

import com.rodrigonovoa.readlog.domain.model.UserProfileInfo

interface UserProfileInfoFirestoreDataSource {
    suspend fun upload(userId: String, info: UserProfileInfo): Result<Unit>
    suspend fun claimUsername(userId: String, info: UserProfileInfo): Result<Unit>
    suspend fun download(userId: String): Result<UserProfileInfo?>
    suspend fun setLike(currentUserId: String, targetUserId: String, liked: Boolean): Result<Boolean>
}
