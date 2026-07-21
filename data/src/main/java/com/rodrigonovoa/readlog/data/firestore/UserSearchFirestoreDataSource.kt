package com.rodrigonovoa.readlog.data.firestore

import com.rodrigonovoa.readlog.domain.model.UserSearchResult

interface UserSearchFirestoreDataSource {
    suspend fun searchByUsernamePrefix(usernameLowerPrefix: String, limit: Int): Result<List<UserSearchResult>>
}
