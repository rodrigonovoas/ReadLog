package com.rodrigonovoa.readlog.data.firestore

import com.rodrigonovoa.readlog.domain.model.UserProfileInfo

interface UserProfileInfoFirestoreDataSource {
    suspend fun upload(userId: String, info: UserProfileInfo): Result<Unit>
    suspend fun download(userId: String): Result<UserProfileInfo?>
}
